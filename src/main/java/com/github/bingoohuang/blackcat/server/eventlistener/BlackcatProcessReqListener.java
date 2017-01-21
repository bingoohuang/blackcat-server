package com.github.bingoohuang.blackcat.server.eventlistener;

import com.github.bingoohuang.blackcat.server.base.BlackcatReqListener;
import com.github.bingoohuang.blackcat.sdk.utils.StrBuilder;
import com.github.bingoohuang.blackcat.server.dao.BlackcatConfig;
import com.github.bingoohuang.blackcat.server.dao.EventDao;
import com.github.bingoohuang.blackcat.server.domain.BlackcatEventLast;
import com.github.bingoohuang.blackcat.server.domain.BlackcatProcessReq;
import com.github.bingoohuang.blackcat.server.job.AbstractMsgJob;
import com.github.bingoohuang.blackcat.server.base.MsgService;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.eventbus.Subscribe;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.bingoohuang.blackcat.server.domain.BlackcatEventType.Process;

@Component
public class BlackcatProcessReqListener implements BlackcatReqListener {
    @Autowired @Qualifier(
            "configProcesses") List<BlackcatConfig.ConfigProcess> configProcesses;

    @Autowired MsgService msgService;
    @Autowired EventDao eventDao;
    @Autowired @Qualifier("times") ConcurrentHashMultiset<String> times;
    @Autowired @Qualifier("beats") ConcurrentHashMap<String, Long> beats;


    @Subscribe
    public void deal(BlackcatProcessReq req) {
        String hostname = req.getHostname();
        times.add(hostname + AbstractMsgJob.PROCESS_TIMES);
        beats.put(hostname, System.currentTimeMillis());

        tryAlert(req);

        eventDao.updateEventLast(new BlackcatEventLast(
                hostname, Process.toString(), req.getTimestamp()
        ));
        eventDao.addProcessesEvent(req);
    }

    private void tryAlert(BlackcatProcessReq req) {
        boolean hasAlert = false;
        val alert = StrBuilder.str('\n').p(req.getHostname());

        for (val confProcess : configProcesses) {
            val hostnames = confProcess.getHostnames();
            if (!hostnames.contains(req.getHostname())) continue;

            val processName = confProcess.getProcessName();
            if (req.getNames().contains(processName)) continue;

            hasAlert = true;
            alert.p("进程").p(processName).p("不存在").p('\n');
        }

        if (!hasAlert) return;

        times.add(req.getHostname() + AbstractMsgJob.PROCESS_ALERTS);
        msgService.sendMsg("进程告警", alert.toString());
    }
}
