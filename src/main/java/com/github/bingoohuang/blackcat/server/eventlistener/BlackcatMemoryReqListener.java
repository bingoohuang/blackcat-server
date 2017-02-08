package com.github.bingoohuang.blackcat.server.eventlistener;

import com.github.bingoohuang.blackcat.sdk.utils.StrBuilder;
import com.github.bingoohuang.blackcat.server.base.BlackcatReqListener;
import com.github.bingoohuang.blackcat.server.base.MsgService;
import com.github.bingoohuang.blackcat.server.dao.EventDao;
import com.github.bingoohuang.blackcat.server.domain.BlackcatConfigBean;
import com.github.bingoohuang.blackcat.server.domain.BlackcatEventLast;
import com.github.bingoohuang.blackcat.server.domain.BlackcatMemoryReq;
import com.github.bingoohuang.blackcat.server.job.AbstractMsgJob;
import com.google.common.eventbus.Subscribe;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.github.bingoohuang.blackcat.sdk.utils.Blackcats.prettyBytes;
import static com.github.bingoohuang.blackcat.server.domain.BlackcatEventType.Memory;

@Component
public class BlackcatMemoryReqListener implements BlackcatReqListener {
    @Autowired MsgService msgService;
    @Autowired EventDao eventDao;
    @Autowired BlackcatConfigBean bean;

    @Subscribe
    public void deal(BlackcatMemoryReq req) {
        val hostname = req.getHostname();
        bean.getTimes().add(hostname + AbstractMsgJob.MEMORY_TIMES);
        bean.getBeats().put(hostname, System.currentTimeMillis());

        tryAlert(req);

        eventDao.updateEventLast(new BlackcatEventLast(
                hostname, Memory.toString(), req.getTimestamp()));
        eventDao.addMemoryEvent(req);
    }

    private void tryAlert(BlackcatMemoryReq req) {
        val avail = req.getMemory().getAvailable();
        val configLowSize = bean.getConfigThreshold().getAvailMemoryLowSize();
        if (avail >= configLowSize) return;

        val alert = StrBuilder.str('\n').p(req.getHostname());
        alert.p("可用内存").p(prettyBytes(avail));
        alert.p(", 低于阀值").p(prettyBytes(configLowSize));

        bean.getTimes().add(req.getHostname() + AbstractMsgJob.MEMORY_ALERTS);
        msgService.sendMsg("内存告警", alert.toString());
    }
}
