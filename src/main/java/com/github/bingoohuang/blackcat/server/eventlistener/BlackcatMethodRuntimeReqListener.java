package com.github.bingoohuang.blackcat.server.eventlistener;

import com.github.bingoohuang.blackcat.sdk.utils.StrBuilder;
import com.github.bingoohuang.blackcat.server.base.BlackcatReqListener;
import com.github.bingoohuang.blackcat.server.base.MsgService;
import com.github.bingoohuang.blackcat.server.dao.BlackcatConfig;
import com.github.bingoohuang.blackcat.server.dao.EventDao;
import com.github.bingoohuang.blackcat.server.domain.BlackcatMethodRuntimeReq;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.bingoohuang.blackcat.sdk.utils.Blackcats.decimal;
import static com.github.bingoohuang.blackcat.server.job.AbstractMsgJob.METHODRUNTIME_ALERTS;
import static com.github.bingoohuang.blackcat.server.job.AbstractMsgJob.METHODRUNTIME_TIMES;

@Component @Slf4j
public class BlackcatMethodRuntimeReqListener implements BlackcatReqListener {
    @Autowired MsgService msgService;
    @Autowired EventDao eventDao;
    @Autowired @Qualifier("times") ConcurrentHashMultiset<String> times;
    @Autowired BlackcatConfig.ConfigThreshold configThreshold;

    @Subscribe
    public void deal(BlackcatMethodRuntimeReq req) {
        try {
            String hostname = req.getHostname();
            times.add(hostname + METHODRUNTIME_TIMES);

            tryAlert(req);

            eventDao.addMethodRuntime(req);
        } catch (Exception e) {
            log.error("deal BlackcatMethodRuntimeReq error", e);
        }
    }

    private void tryAlert(BlackcatMethodRuntimeReq req) {
        val methodMaxMillis = configThreshold.getMethodMaxMillis();
        val hasAlert = req.getRt().getCostNano() > methodMaxMillis * 1000000;
        if (!hasAlert) return;

        StrBuilder alert = StrBuilder.str('\n').p(req.getHostname());
        alert.p('\n').p(req.rt.getClassName());
        alert.p('\n').p(simpleMethodName(req.rt.getMethodDesc()));
        String cost = decimal(req.rt.getCostNano() / 1000000.) + "ms";

        times.add(req.getHostname() + METHODRUNTIME_ALERTS);
        msgService.sendMsg("方法耗时" + cost, alert.toString());
    }

    static Pattern classMethod = Pattern.compile("(?<=\\.)(\\w+\\.\\w+)(?=\\()");

    private static String simpleMethodName(String source) {
        Matcher matcher = classMethod.matcher(source);
        matcher.find();
        return matcher.group();
    }
}
