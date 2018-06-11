package cn.zhonggu.barsf.iota.explorer.controllers;

import cn.zhonggu.barsf.iota.explorer.dao.models.Transaction;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zeromq.ZMQ;

import javax.annotation.Resource;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static cn.zhonggu.barsf.iota.explorer.controllers.ReplyController.asyncContexts;

@Controller
public class ReplyController {
    private static final Logger log = LoggerFactory.getLogger(ReplyController.class);
    // 异步连接容器
    public static Map<String, AsyncContext> asyncContexts = new ConcurrentHashMap<String, AsyncContext>();

    @Resource
    private TxLiveRunner txLiveRunner;

    @RequestMapping("/")
    public String toIndex() {
        return "index";
    }

    @RequestMapping("/live-transactions")
    public String toTxLive(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return "business/txlive";
    }

    @RequestMapping(value = "/sse-live", produces = "text/event-stream")
    public void sseLiveTransaction(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if ("text/event-stream".equals(request.getHeader("Accept"))) {

            if (!txLiveRunner.isRunning()) {
                txLiveRunner.runIt();
            }
            request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);

            response.setContentType("text/event-stream");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            response.setCharacterEncoding("UTF-8");

            final String id = UUID.randomUUID().toString();

            // 数量太多会导致大量消耗服务器资源, 可能调整
            final AsyncContext ac = request.startAsync();
            // 对于每一个连接 最高持续60分钟
            ac.setTimeout(60*60*1000);
            ac.addListener(new AsyncListener() {

                @Override
                public void onComplete(AsyncEvent event) throws IOException {
                    asyncContexts.remove(id);
                }

                @Override
                public void onError(AsyncEvent event) throws IOException {
                    asyncContexts.remove(id);
                }

                @Override
                public void onStartAsync(AsyncEvent event) throws IOException {
                   // do nothing
                }

                @Override
                public void onTimeout(AsyncEvent event) throws IOException {
                    asyncContexts.remove(id);
                }
            });

            asyncContexts.put(id, ac);
            for (Transaction oneTrans : txLiveRunner.lastTrans) {
                txLiveRunner.sendMessage(response.getWriter(), oneTrans);
            }
        }
    }


}

@Service
class TxLiveRunner {
    private static final String SUBSCRIBE_URL = "tcp://35.229.212.138:5556";
    private volatile boolean running = false;
    private static final int liveCacheSize = 10;

    private static final Logger log = LoggerFactory.getLogger(TxLiveRunner.class);
    // 推送信息队列
    private static final BlockingQueue<Transaction> tx_live_queue = new LinkedBlockingQueue<>(liveCacheSize);
    public final Queue<Transaction> lastTrans = new ArrayBlockingQueue<Transaction>(liveCacheSize);

    private static final Gson GSON = new Gson();

    public void shutdown() {
        this.running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public void runIt() {
        this.running = true;
        new Thread(() -> {
            log.info("started ZMQ data receiver");


            ZMQ.Context context = ZMQ.context(1);
            ZMQ.Socket requester = context.socket(ZMQ.SUB);
            requester.connect(SUBSCRIBE_URL);
            requester.subscribe("tx ");

            while (running) {
                byte[] reply = requester.recv(0);
                String[] infos = new String(reply).split(" ");
                String hash = infos[1];
                String address = infos[2];
                String value = infos[3];
                String timestamp = infos[5];   // 推过来的时间戳有问题... 尴尬
                String arrivalTime = infos[11];

                if (Long.parseLong(value) != 0) {
                    Transaction temp = new Transaction();
                    temp.setHash(hash);
                    temp.setAddress(address);
                    temp.setValue(Long.parseLong(value));
                    temp.setTimestamp(Long.parseLong(timestamp));
                    temp.setArrivalTime(Long.parseLong(arrivalTime));

                    boolean add = tx_live_queue.offer(temp);
                    if (!add) {
                        log.error(" TX_LIVE_QUEUE is full, drop:" + hash);
                    }
                    if (add){
                        if (lastTrans.size() >= liveCacheSize){
                            lastTrans.remove();
                        }
                        lastTrans.offer(temp);
                    }
                }
            }
        }).start();

        new Thread(() -> {
            log.info("started Live-transaction sse push");
            while (running) {
                try {
                    // 让程序看起来更平滑...
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {

                    }
                    Transaction oneTrans = tx_live_queue.take();

                    for (AsyncContext asyncContext : asyncContexts.values()) {
                        try {
                            sendMessage(asyncContext.getResponse().getWriter(), oneTrans);
                        } catch (Exception e) {
                            asyncContexts.values().remove(asyncContext);
                        }
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }).start();
    }

    /**
     * Sends messages to client in SSE format.
     *
     * @param writer
     * @param trans
     */
    private static int Id = 0;

    public void sendMessage(PrintWriter writer, Transaction trans) {
//        writer.print("id: ");
//        writer.println(Id++);
        if (Id > 999999) {
            Id = 0;
            log.debug(" current content size:" + asyncContexts.size());
        }
        writer.println("event: live");
        writer.print("data: ");
        writer.println(GSON.toJson(trans));
        writer.println();
//        writer.flush();
        boolean otherSideClosed = writer.checkError();
        if (otherSideClosed) {
            writer.close();
        }
    }
}

