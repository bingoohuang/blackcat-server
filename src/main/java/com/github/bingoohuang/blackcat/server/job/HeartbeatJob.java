package com.github.bingoohuang.blackcat.server.job;

import com.github.bingoohuang.blackcat.sdk.utils.StrBuilder;
import com.github.bingoohuang.blackcat.server.base.BlackcatJob;
import com.github.bingoohuang.blackcat.server.base.MsgService;
import com.github.bingoohuang.blackcat.server.base.QuartzScheduler;
import com.github.bingoohuang.blackcat.server.domain.BlackcatConfigBean;
import lombok.SneakyThrows;
import lombok.val;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentMap;

import static com.github.bingoohuang.blackcat.sdk.utils.Blackcats.format;
import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Component
public class HeartbeatJob implements BlackcatJob {
    @Autowired MsgService msgService;
    @Autowired BlackcatConfigBean configBean;

    @SneakyThrows @Override
    public void scheduleJob(QuartzScheduler scheduler) {
        if (configBean.getConfigHostnames().isEmpty()) return;

        val heartbeatJob = createHeartbeatJobDetail();
        val heartbeatTrigger = createHeartbeatTrigger();
        scheduler.scheduleJob(heartbeatJob, heartbeatTrigger);
    }

    private JobDetail createHeartbeatJobDetail() {
        val job = newJob(HeartbeatQuartzJob.class).build();
        job.getJobDataMap().put("handler", this);
        return job;
    }

    private Trigger createHeartbeatTrigger() {
        return newTrigger().withSchedule(
                simpleSchedule()
                        .withIntervalInMinutes(1)
                        .repeatForever())
                .startAt(futureDate(70, DateBuilder.IntervalUnit.SECOND))
                .build();
    }

    public static class HeartbeatQuartzJob implements Job {
        @Override @SneakyThrows
        public void execute(JobExecutionContext context) {
            val jobDataMap = context.getJobDetail().getJobDataMap();
            val handler = (HeartbeatJob) jobDataMap.get("handler");

            handler.checkHeartbeats();
        }
    }

    private void checkHeartbeats() {
        StrBuilder msg = new StrBuilder();
        ConcurrentMap<String, Long> beats = configBean.getBeats();

        for (String hostname : configBean.getConfigHostnames()) {
            Long lastBeat = beats.get(hostname);
            if (lastBeat != null && isLastBeatInOneMinute(lastBeat)) continue;

            msg.p("\n").p(hostname);

            if (lastBeat == null) msg.p("没有检测到心跳");
            else msg.p("上次心跳").p(format(lastBeat)).p("在1分钟前");
        }

        if (msg.len() == 0) return;

        msgService.sendMsg("服务器心跳告警", msg.toString());
    }


    private boolean isLastBeatInOneMinute(long lastBeat) {
        return System.currentTimeMillis() - lastBeat <= 60000;
    }

}
