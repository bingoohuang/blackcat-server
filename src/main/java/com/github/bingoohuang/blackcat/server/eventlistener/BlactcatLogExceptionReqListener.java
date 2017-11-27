package com.github.bingoohuang.blackcat.server.eventlistener;

import com.github.bingoohuang.blackcat.server.base.BlackcatReqListener;
import com.github.bingoohuang.blackcat.server.base.MsgService;
import com.github.bingoohuang.blackcat.server.dao.EventDao;
import com.github.bingoohuang.blackcat.server.domain.BlackcatLogExceptionReq;
import com.google.common.eventbus.Subscribe;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlactcatLogExceptionReqListener implements BlackcatReqListener {
    @Autowired MsgService msgService;
    @Autowired EventDao eventDao;

    @Subscribe
    public void deal(BlackcatLogExceptionReq req) {
        val msg = new StringBuilder()
                .append("Host:").append(req.getHostname())
                .append("\nTs:").append(req.getTimestamp())
                .append("\nLogger:").append(req.getLogger())
                .append("\nTcode:").append(req.getTcode())
                .append("\nTid:").append(req.getTid())
                .append("\nEx:").append(req.getExceptionNames())
                .append("\nLogId:").append(req.getLogId());

        msgService.sendMsg("日志中发现异常", msg.toString());
        eventDao.addLogException(req);
    }
}
