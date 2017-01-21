package com.github.bingoohuang.blackcat.server.eventlistener;

import com.github.bingoohuang.blackcat.server.dao.EventDao;
import com.github.bingoohuang.blackcat.server.domain.BlackcatTraceReq;
import com.github.bingoohuang.blackcat.server.base.BlackcatReqListener;
import com.google.common.eventbus.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlackcatTraceReqListener implements BlackcatReqListener {
    @Autowired EventDao eventDao;

    @Subscribe
    public void deal(BlackcatTraceReq req) {
        eventDao.updateEventTrace(req);
    }
}
