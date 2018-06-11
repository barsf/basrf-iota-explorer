package cn.zhonggu.barsf.iota.explorer.services;

import cn.zhonggu.barsf.iota.explorer.dao.mapper.MilestoneMapper;
import cn.zhonggu.barsf.iota.explorer.dao.models.Milestone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

@Scope("singleton")
@Component
public class MilestoneTracer {
    private static final Logger log = LoggerFactory.getLogger(MilestoneTracer.class);
    @Autowired
    private MilestoneMapper msMapper;

    private static boolean stopRun = false;
    private static int INTERVAL_TIME = 0; // second
    private static String nowIndex = "0";
    public final LinkedHashSet<String> MILESTONE_SET = new LinkedHashSet<>();

    // 该方法会在bean被初始化后执行
    @PostConstruct
    public void traceMilestone() {
        new Thread(() -> {
            while (!stopRun) {
                try {
                    // 不停地更新milestone列表
                    Milestone theNext = msMapper.next(nowIndex);
                    if (theNext!=null) {
                        MILESTONE_SET.add(theNext.getHash());
                        nowIndex = theNext.getIndex() + "";
                    } else {
                        INTERVAL_TIME = 2;
                    }
                    TimeUnit.SECONDS.sleep(INTERVAL_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    log.error("",e);
                }
            }
        }).start();
    }

}


