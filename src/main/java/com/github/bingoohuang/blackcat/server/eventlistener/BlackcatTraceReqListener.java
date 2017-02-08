package com.github.bingoohuang.blackcat.server.eventlistener;

import com.alibaba.fastjson.JSON;
import com.github.bingoohuang.blackcat.server.base.BlackcatReqListener;
import com.github.bingoohuang.blackcat.server.base.MsgService;
import com.github.bingoohuang.blackcat.server.dao.EventDao;
import com.github.bingoohuang.blackcat.server.domain.BlackcatTraceReq;
import com.google.common.eventbus.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlackcatTraceReqListener implements BlackcatReqListener {
    @Autowired EventDao eventDao;
    @Autowired MsgService msgService;

    @Subscribe
    public void deal(BlackcatTraceReq req) {
        if ("Exception".equals(req.getMsgType())) {
            String traceLink = req.getTraceId() + ":" + req.getLinkId();
            String errorMsg = getErrorMessage(req);
            msgService.sendMsg("Exception[" + traceLink + "]", errorMsg);
        }

        eventDao.updateEventTrace(req);
    }

    private String getErrorMessage(BlackcatTraceReq req) {
        Object object;
        try {
            object = JSON.parse(req.getMsg());
        } catch (Exception e) {
            return req.getMsg();
        }

        if (object instanceof Throwable) {
            return ((Throwable) object).getMessage();
        }
        return req.getMsg();
    }
}
