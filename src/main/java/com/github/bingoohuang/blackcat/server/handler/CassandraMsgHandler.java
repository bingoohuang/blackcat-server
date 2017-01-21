package com.github.bingoohuang.blackcat.server.handler;

import com.github.bingoohuang.blackcat.server.base.BlackcatEventReq;
import com.github.bingoohuang.blackcat.server.dao.BlackcatConfig;
import com.github.bingoohuang.blackcat.server.domain.BlackcatMemoryReq;
import com.github.bingoohuang.blackcat.sdk.BlackcatMsgHandler;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatRsp;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatRspHead;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatRspHead.RspType;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatWarnConfig;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatWarnConfig.BlackcatWarnProcess;
import com.github.bingoohuang.blackcat.sdk.utils.Blackcats;
import com.github.bingoohuang.blackcat.server.base.BlackcatJob;
import com.github.bingoohuang.blackcat.server.base.BlackcatReqListener;
import com.github.bingoohuang.blackcat.server.base.QuartzScheduler;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component @Slf4j
public class CassandraMsgHandler implements BlackcatMsgHandler, ApplicationContextAware {
    @Autowired QuartzScheduler scheduler;
    @Autowired @Qualifier("configProcesses") List<BlackcatConfig.ConfigProcess> configProcs;
    EventBus reqEventBus = new EventBus();
    private ApplicationContext appContext;

    @SneakyThrows
    public void scheduleJob() {
        val reqListeners = appContext.getBeansOfType(BlackcatJob.class);
        for (BlackcatJob job : reqListeners.values()) {
            job.scheduleJob(scheduler);
        }
    }

    Cache<String, ChannelHandlerContext> ctxCache = CacheBuilder
            .newBuilder().weakValues().build();

    AttributeKey<String> channelHostname = AttributeKey.valueOf("channelHostname");

    @PostConstruct
    public void postConstruct() {
        registerToEventBus();
        scheduleJob();
    }

    private void registerToEventBus() {
        val reqListeners = appContext.getBeansOfType(BlackcatReqListener.class);
        for (Object subscriber : reqListeners.values()) {
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

        val hostname = req.getBlackcatReqHead().getHostname();
        val present = ctxCache.getIfPresent(hostname);
        if (present == null) {
            ctxCache.put(hostname, ctx);

            val attr = ctx.attr(channelHostname);
            attr.set(hostname);
            ctx.write(createConfigRsp(hostname));
        }
    }

    private BlackcatRsp createConfigRsp(String hostname) {
        val builder = BlackcatWarnConfig.newBuilder();
        for (val configProcess : configProcs) {
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

    private BlackcatEventReq process(BlackcatReq req) throws Exception {
        val packageName = BlackcatMemoryReq.class.getPackage().getName();
        Object instance = Blackcats.parseReq(packageName, req);
        if (instance != null) reqEventBus.post(instance);

        return (BlackcatEventReq) instance;
    }

    @Override
    public void setApplicationContext(
            ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }
}
