package com.github.bingoohuang.blackcat.server.eventlistener;

import com.github.bingoohuang.blackcat.sdk.utils.StrBuilder;
import com.github.bingoohuang.blackcat.server.base.BlackcatReqListener;
import com.github.bingoohuang.blackcat.server.base.MsgService;
import com.github.bingoohuang.blackcat.server.dao.EventDao;
import com.github.bingoohuang.blackcat.server.domain.BlackcatConfigBean;
import com.github.bingoohuang.blackcat.server.domain.BlackcatEventLast;
import com.github.bingoohuang.blackcat.server.domain.BlackcatLoadReq;
import com.github.bingoohuang.blackcat.server.job.AbstractMsgJob;
import com.google.common.eventbus.Subscribe;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.github.bingoohuang.blackcat.server.domain.BlackcatEventType.Load;
import static java.lang.Math.min;

@Component
public class BlackcatLoadReqListener implements BlackcatReqListener {
    @Autowired MsgService msgService;
    @Autowired EventDao eventDao;
    @Autowired BlackcatConfigBean bean;

    @Subscribe
    public void deal(BlackcatLoadReq req) {
        String hostname = req.getHostname();
        bean.getTimes().add(hostname + AbstractMsgJob.LOAD_TIMES);
        bean.getBeats().put(hostname, System.currentTimeMillis());

        tryAlert(req);

        eventDao.updateEventLast(new BlackcatEventLast(
                hostname, Load.toString(), req.getTimestamp()));
        eventDao.addLoadEvent(req);
    }

    private void tryAlert(BlackcatLoadReq req) {
        val load = req.getLoad();
        int confRatio = bean.getConfigThreshold().getLoadOverRatio();
        int ratio = (int) (load.getFiveMinsAvg() * 100 / load.getCpuNum());
        if (ratio - 100 <= confRatio) return;

        val topProcessList = load.getTopProcessList();
        val alert = StrBuilder.str('\n').p(req.getHostname())
                .p("的5分钟负载").p(load.getFiveMinsAvg())
                .p("超过告警阈值").p(confRatio).p('%')
                .p('\n')
                .p(topProcessList.subList(0, min(topProcessList.size(), 3)));

        bean.getTimes().add(req.getHostname() + AbstractMsgJob.LOAD_ALERTS);
        msgService.sendMsg("负载告警", alert.toString());
    }
}
