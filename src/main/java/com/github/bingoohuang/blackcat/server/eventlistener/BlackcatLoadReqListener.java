package com.github.bingoohuang.blackcat.server.eventlistener;

import com.github.bingoohuang.blackcat.server.base.BlackcatReqListener;
import com.github.bingoohuang.blackcat.sdk.utils.StrBuilder;
import com.github.bingoohuang.blackcat.server.dao.BlackcatConfig;
import com.github.bingoohuang.blackcat.server.dao.EventDao;
import com.github.bingoohuang.blackcat.server.domain.BlackcatEventLast;
import com.github.bingoohuang.blackcat.server.domain.BlackcatLoadReq;
import com.github.bingoohuang.blackcat.server.job.AbstractMsgJob;
import com.github.bingoohuang.blackcat.server.base.MsgService;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.eventbus.Subscribe;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

import static com.github.bingoohuang.blackcat.server.domain.BlackcatEventType.Load;

@Component
public class BlackcatLoadReqListener implements BlackcatReqListener {
    @Autowired MsgService msgService;
    @Autowired EventDao eventDao;
    @Autowired @Qualifier("times") ConcurrentHashMultiset<String> times;
    @Autowired @Qualifier("beats") ConcurrentHashMap<String, Long> beats;
    @Autowired BlackcatConfig.ConfigThreshold configThreshold;

    @Subscribe
    public void deal(BlackcatLoadReq req) {
        String hostname = req.getHostname();
        times.add(hostname + AbstractMsgJob.LOAD_TIMES);
        beats.put(hostname, System.currentTimeMillis());

        tryAlert(req);

        eventDao.updateEventLast(new BlackcatEventLast(
                hostname, Load.toString(), req.getTimestamp()));
        eventDao.addLoadEvent(req);
    }

    private void tryAlert(BlackcatLoadReq req) {
        val load = req.getLoad();
        int confRatio = configThreshold.getLoadOverRatio();
        int ratio = (int) (load.getFiveMinsAvg() * 100 / load.getCpuNum());
        if (ratio - 100 <= confRatio) return;

        val alert = StrBuilder.str('\n').p(req.getHostname())
                .p("的5分钟负载").p(load.getFiveMinsAvg())
                .p("超过告警阈值").p(confRatio).p('%');

        times.add(req.getHostname() + AbstractMsgJob.LOAD_ALERTS);
        msgService.sendMsg("负载告警", alert.toString());
    }
}
