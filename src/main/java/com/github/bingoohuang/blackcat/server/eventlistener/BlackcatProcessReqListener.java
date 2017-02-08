package com.github.bingoohuang.blackcat.server.eventlistener;

import com.github.bingoohuang.blackcat.sdk.utils.StrBuilder;
import com.github.bingoohuang.blackcat.server.base.BlackcatReqListener;
import com.github.bingoohuang.blackcat.server.base.MsgService;
import com.github.bingoohuang.blackcat.server.dao.EventDao;
import com.github.bingoohuang.blackcat.server.domain.BlackcatConfigBean;
import com.github.bingoohuang.blackcat.server.domain.BlackcatEventLast;
import com.github.bingoohuang.blackcat.server.domain.BlackcatProcessReq;
import com.github.bingoohuang.blackcat.server.job.AbstractMsgJob;
import com.google.common.eventbus.Subscribe;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.github.bingoohuang.blackcat.server.domain.BlackcatEventType.Process;

@Component
public class BlackcatProcessReqListener implements BlackcatReqListener {
    @Autowired BlackcatConfigBean bean;
    @Autowired MsgService msgService;
    @Autowired EventDao eventDao;


    @Subscribe
    public void deal(BlackcatProcessReq req) {
        String hostname = req.getHostname();
        bean.getTimes().add(hostname + AbstractMsgJob.PROCESS_TIMES);
        bean.getBeats().put(hostname, System.currentTimeMillis());

        tryAlert(req);

        eventDao.updateEventLast(new BlackcatEventLast(
                hostname, Process.toString(), req.getTimestamp()
        ));
        eventDao.addProcessesEvent(req);
    }

    private void tryAlert(BlackcatProcessReq req) {
        boolean hasAlert = false;
        val alert = StrBuilder.str('\n').p(req.getHostname());

        for (val confProcess : bean.getConfigProcesses()) {
            val hostnames = confProcess.getHostnames();
            if (!hostnames.contains(req.getHostname())) continue;

            val processName = confProcess.getProcessName();
            if (req.getNames().contains(processName)) continue;

            hasAlert = true;
            alert.p("进程").p(processName).p("不存在").p('\n');
        }

        if (!hasAlert) return;

        bean.getTimes().add(req.getHostname() + AbstractMsgJob.PROCESS_ALERTS);
        msgService.sendMsg("进程告警", alert.toString());
    }
}
