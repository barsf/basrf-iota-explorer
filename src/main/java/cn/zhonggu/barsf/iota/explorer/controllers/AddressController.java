package cn.zhonggu.barsf.iota.explorer.controllers;

import cn.zhonggu.barsf.iota.explorer.dao.models.Address;
import cn.zhonggu.barsf.iota.explorer.dao.models.Transaction;
import cn.zhonggu.barsf.iota.explorer.services.AddressService;
import cn.zhonggu.barsf.iota.explorer.services.TransactionService;
import cn.zhonggu.barsf.iota.explorer.utils.TransactionHelper;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ZhuDH on 2018/4/3.
 */
@Controller
@RequestMapping("/addr")
public class AddressController {

    @Resource
    private AddressService addressService;
    @Resource
    private TransactionService transactionService;

    public static void main(String[] args) {
        List<Transaction> attachTrans = new ArrayList<>();
        Transaction t1 = new Transaction();
        t1.setAttachmentTimestamp(123L);
        t1.setHash("1");
        Transaction t2 = new Transaction();
        t2.setAttachmentTimestamp(124L);
        t2.setHash("2");
        attachTrans.add(t2);
        attachTrans.add(t1);
        attachTrans.sort((Transaction o2, Transaction o1) -> {
            long cha = o1.getAttachmentTimestamp() == 0 ? Long.parseLong((o1.getTimestamp() + "000").substring(0, 13)) : o1.getAttachmentTimestamp()
                    - (o2.getAttachmentTimestamp() == 0 ? Long.parseLong((o2.getTimestamp() + "000").substring(0, 13)) : o2.getAttachmentTimestamp());
            return Long.compare(cha, 0);
        });
        for (Transaction attachTran : attachTrans) {
            System.out.println(attachTran.getHash());
        }

    }

    @ResponseBody
    @RequestMapping("/detail/{addrhash}")
    public HashMap<String, Object> getAddressInfo(@PathVariable("addrhash") String addressHash) {
        String realHash = TransactionHelper.cutEveryTo81(addressHash);
        AtomicBoolean checkOk = new AtomicBoolean(true);
        HashSet<Integer> confirmList = new HashSet<>();
        List<Transaction> attachTrans = transactionService.getTransactionByAddress(realHash);
        Address theAddress = addressService.getAddressByPk(addressHash);
        if (theAddress == null) {
            theAddress = new Address();
            checkOk.set(false);
        }


        int numberOfTran = 0;
        long received = 0;
        long sent = 0;

        attachTrans.sort((o1, o2) -> {
            long cha = o1.getAttachmentTimestamp() == 0 ? Long.parseLong((o1.getTimestamp() + "000").substring(0, 13)) : o1.getAttachmentTimestamp()
                    - (o2.getAttachmentTimestamp() == 0 ? Long.parseLong((o2.getTimestamp() + "000").substring(0, 13)) : o2.getAttachmentTimestamp());
            return Long.compare(cha, 0);
        });

        for (Transaction attachTran : attachTrans) {
            numberOfTran++;
            if (attachTran.snapshot > 0) {
                confirmList.add((attachTran.value + "WTF" + attachTran.bundle).hashCode());
                if (attachTran.getValue() < 0) {
                    sent += attachTran.getValue();
                } else {
                    received += attachTran.getValue();
                }
            }
        }


        for (Transaction attachTran : attachTrans) {
            if (attachTran.snapshot == 0) {
                if (confirmList.contains((attachTran.value + "WTF" + attachTran.bundle).hashCode())) {
                    // reattachement 约定 -1
                    attachTran.snapshot = -1;
                }
            }
        }

        HashMap<String, Object> rv = new HashMap<>();
        rv.put("numberoftran", numberOfTran);
        rv.put("received", received);
        rv.put("sent", sent);
        rv.put("balance", (received - sent));
        rv.put("list", attachTrans);
        rv.put("cut", attachTrans.size() >= 100);
        rv.put("checkok", checkOk.get());

        return rv;

    }

    @RequestMapping("/{addrhash}")
    public String getAddress(Model model, @PathVariable("addrhash") String addressHash) {
        String realHash = TransactionHelper.cutEveryTo81(addressHash);

        model.addAttribute("pk", realHash);
        return "business/addrinfo";
    }


    @ResponseBody
    @RequestMapping("/getIsUsed/{addrhash}")
    public Map<String, Object> getIsUsed(@PathVariable("addrhash") String addressHash) throws Exception {
        HttpClient client = new DefaultHttpClient();
        // 设置代理服务器地址和端口
        //client.getHostConfiguration().setProxy("proxy_host_addr",proxy_port)
        //使用POST方法
        HttpPost method = new HttpPost("http://35.229.212.138:14666");
        method.addHeader("Content-Type","application/json");
        method.addHeader("X-IOTA-API-Version","1");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("command","wereAddressesSpentFrom");
        map.put("addresses",new String[]{addressHash});
        Gson gson = new Gson();
        StringEntity entity = new StringEntity(gson.toJson(map));
        method.setEntity(entity);
        HttpResponse res = client.execute(method);
        map.clear();
        if(res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String result = EntityUtils.toString(res.getEntity());// 返回json格式
            if (result.indexOf("states")> -1 ){
                String states = result.substring(result.indexOf("[")+1,result.indexOf("]"));
                map.put("used", Boolean.valueOf(states)?"Yes":"No");
            }
        }
        return  map;
    }

    @ResponseBody
    @RequestMapping("/getBalance/{addrhash}")
    public Map<String, Object> getBalance(@PathVariable("addrhash") String addressHash) throws Exception {
        HttpClient client = new DefaultHttpClient();
        // 设置代理服务器地址和端口
        //client.getHostConfiguration().setProxy("proxy_host_addr",proxy_port)
        //使用POST方法
        HttpPost method = new HttpPost("http://35.229.212.138:14666");
        method.addHeader("Content-Type","application/json");
        method.addHeader("X-IOTA-API-Version","1");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("command","getBalances");
        map.put("addresses",new String[]{addressHash});
        map.put("threshold",100);
        Gson gson = new Gson();
        StringEntity entity = new StringEntity(gson.toJson(map));
        method.setEntity(entity);
        HttpResponse res = client.execute(method);
        map.clear();
        if(res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String result = EntityUtils.toString(res.getEntity());// 返回json格式
            if (result.indexOf("balances")> -1 ){
                String balance = result.substring(result.indexOf("[")+1,result.indexOf("]")).replace("\"","");
                map.put("balance", Long.parseLong(balance));
            }
        }
        return  map;
    }
}
