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
public class BlackcatLogExceptionReqListener implements BlackcatReqListener {
    @Autowired MsgService msgService;
    @Autowired EventDao eventDao;

    @Subscribe
    public void deal(BlackcatLogExceptionReq req) {
        val msg = new StringBuilder()
                .append("Host:").append(req.getHostname())
                .append("\nTs:").append(req.getTimestamp())
                .append("\nLogger:").append(req.getLogger())
                .append("\nTcode:").append(req.getTcode())
                .append("\nLogId:").append("<a href=\"http://hbm.easy-hi.cn/blackcat-detail/log/" + req.getLogId() + "\">" + req.getLogId() + "</a>")
                .append("\nEx:").append(req.getExceptionNames());

        msgService.sendMsg("日志异常", msg.toString());
        eventDao.addLogException(req);
    }
}
