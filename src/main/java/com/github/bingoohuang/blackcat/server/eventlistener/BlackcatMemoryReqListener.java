package com.github.bingoohuang.blackcat.server.eventlistener;

import com.github.bingoohuang.blackcat.server.base.BlackcatReqListener;
import com.github.bingoohuang.blackcat.sdk.utils.StrBuilder;
import com.github.bingoohuang.blackcat.server.dao.BlackcatConfig;
import com.github.bingoohuang.blackcat.server.dao.EventDao;
import com.github.bingoohuang.blackcat.server.domain.BlackcatEventLast;
import com.github.bingoohuang.blackcat.server.domain.BlackcatMemoryReq;
import com.github.bingoohuang.blackcat.server.job.AbstractMsgJob;
import com.github.bingoohuang.blackcat.server.base.MsgService;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.eventbus.Subscribe;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

import static com.github.bingoohuang.blackcat.sdk.utils.Blackcats.prettyBytes;
import static com.github.bingoohuang.blackcat.server.domain.BlackcatEventType.Memory;

@Component
public class BlackcatMemoryReqListener implements BlackcatReqListener {
    @Autowired MsgService msgService;
    @Autowired EventDao eventDao;
    @Autowired @Qualifier("times") ConcurrentHashMultiset<String> times;
    @Autowired @Qualifier("beats") ConcurrentHashMap<String, Long> beats;
    @Autowired BlackcatConfig.ConfigThreshold configThreshold;

    @Subscribe
    public void deal(BlackcatMemoryReq req) {
        val hostname = req.getHostname();
        times.add(hostname + AbstractMsgJob.MEMORY_TIMES);
        beats.put(hostname, System.currentTimeMillis());

        tryAlert(req);

        eventDao.updateEventLast(new BlackcatEventLast(
                hostname, Memory.toString(), req.getTimestamp()));
        eventDao.addMemoryEvent(req);
    }

    private void tryAlert(BlackcatMemoryReq req) {
        val avail = req.getMemory().getAvailable();
        val configLowSize = configThreshold.getAvailMemoryLowSize();
        if (avail >= configLowSize) return;

        val alert = StrBuilder.str('\n').p(req.getHostname());
        alert.p("可用内存").p(prettyBytes(avail));
        alert.p(", 低于告警阀值").p(prettyBytes(configLowSize));

        times.add(req.getHostname() + AbstractMsgJob.MEMORY_ALERTS);
        msgService.sendMsg("内存告警", alert.toString());
    }
}
