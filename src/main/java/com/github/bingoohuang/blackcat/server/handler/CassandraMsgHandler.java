package com.github.bingoohuang.blackcat.server.handler;

import com.github.bingoohuang.blackcat.sdk.BlackcatMsgHandler;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatRsp;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatRspHead;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatRspHead.RspType;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatWarnConfig;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatWarnConfig.BlackcatWarnProcess;
import com.github.bingoohuang.blackcat.sdk.utils.Blackcats;
import com.github.bingoohuang.blackcat.server.base.BlackcatEventReq;
import com.github.bingoohuang.blackcat.server.base.BlackcatJob;
import com.github.bingoohuang.blackcat.server.base.BlackcatReqListener;
import com.github.bingoohuang.blackcat.server.base.QuartzScheduler;
import com.github.bingoohuang.blackcat.server.domain.BlackcatConfigBean;
import com.github.bingoohuang.blackcat.server.domain.BlackcatMemoryReq;
import com.google.common.eventbus.EventBus;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component @Slf4j
public class CassandraMsgHandler implements BlackcatMsgHandler, ApplicationContextAware {
    @Autowired QuartzScheduler scheduler;
    @Autowired BlackcatConfigBean configBean;
    EventBus reqEventBus = new EventBus();
    private ApplicationContext appContext;

    @SneakyThrows
    public void scheduleJob() {
        val reqListeners = appContext.getBeansOfType(BlackcatJob.class);
        for (val job : reqListeners.values()) {
            job.scheduleJob(scheduler);
        }
    }

    public static final AttributeKey<String> CHANNEL_HOSTNAME = AttributeKey.valueOf("channelHostname");

    @PostConstruct
    public void postConstruct() {
        registerToEventBus();
        scheduleJob();
    }

    private void registerToEventBus() {
        val reqListeners = appContext.getBeansOfType(BlackcatReqListener.class);
        for (val subscriber : reqListeners.values()) {
            reqEventBus.register(subscriber);
        }
    }

    @Override
    public void handle(BlackcatReq req, ChannelHandlerContext ctx) {
        try {
            val eventReq = process(req);
            sendConfigToAgent(eventReq, req, ctx);
        } catch (Exception e) {
            log.warn("error", e);
        }
    }

    private void sendConfigToAgent(BlackcatEventReq eventReq,
                                   BlackcatReq req,
                                   ChannelHandlerContext ctx) {
        if (eventReq == null || !eventReq.isFromBlackcatAgent()) return;

        val attr = ctx.attr(CHANNEL_HOSTNAME);
        if (StringUtils.isEmpty(attr.get())) {
            val hostname = req.getBlackcatReqHead().getHostname();
            attr.set(hostname);
            ctx.write(createConfigRsp(hostname));
        }
    }

    private BlackcatRsp createConfigRsp(String hostname) {
        val builder = BlackcatWarnConfig.newBuilder();
        for (val configProcess : configBean.getConfigProcesses()) {
            if (!configProcess.getHostnames().contains(hostname)) continue;

            val warnProcess = BlackcatWarnProcess.newBuilder()
                    .setProcessName(configProcess.getProcessName())
                    .addAllProcessKeys(configProcess.getArgKeys())
                    .build();
            builder.addBlackcatWarnProcess(warnProcess);
        }

        return BlackcatRsp.newBuilder()
                .setBlackcatRspHead(BlackcatRspHead.newBuilder()
                        .setRspType(RspType.BlackcatWarnConfig))
                .setBlackcatWarnConfig(builder)
                .build();
    }

    public static final String REQ_PACKAGE_NAME = BlackcatMemoryReq.class.getPackage().getName();

    private BlackcatEventReq process(BlackcatReq req) throws Exception {
        log.debug("received req:{}", req);
        val instance = Blackcats.parseReq(REQ_PACKAGE_NAME, req);
        log.debug("parsed to instance: {}", instance);

        if (instance != null) reqEventBus.post(instance);

        return (BlackcatEventReq) instance;
    }

    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        this.appContext = appContext;
    }
}
