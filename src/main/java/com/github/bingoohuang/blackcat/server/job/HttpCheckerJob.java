package com.github.bingoohuang.blackcat.server.job;

import com.github.bingoohuang.blackcat.server.base.BlackcatJob;
import com.github.bingoohuang.blackcat.server.base.MsgService;
import com.github.bingoohuang.blackcat.server.base.QuartzScheduler;
import com.github.bingoohuang.utils.net.HttpReq;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.INDEX_NOT_FOUND;
import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Component
public class HttpCheckerJob implements BlackcatJob {
    @Autowired MsgService msgService;

    @SneakyThrows
    public void scheduleJob(QuartzScheduler scheduler) {
        val heartbeatJob = createHeartbeatJobDetail();
        val heartbeatTrigger = createHeartbeatTrigger();
        scheduler.scheduleJob(heartbeatJob, heartbeatTrigger);
    }

    private JobDetail createHeartbeatJobDetail() {
        val job = newJob(HttpCheckerQuartzJob.class).build();
        job.getJobDataMap().put("handler", this);
        return job;
    }

    private Trigger createHeartbeatTrigger() {
        return newTrigger().withSchedule(
                simpleSchedule()
                        .withIntervalInMinutes(10) // 先写死10分钟
                        .repeatForever())
                .startAt(futureDate(70, DateBuilder.IntervalUnit.SECOND))
                .build();
    }

    public static class HttpCheckerQuartzJob implements Job {
        @Override @SneakyThrows
        public void execute(JobExecutionContext context) {
            val jobDataMap = context.getJobDetail().getJobDataMap();
            val handler = (HttpCheckerJob) jobDataMap.get("handler");

            handler.checkAllHttps();
        }
    }

    @SneakyThrows
    private void checkAllHttps() {
        File urlFile = new File("httpchecker.urls");
        if (urlFile.exists() && urlFile.isFile()) {
            val lines = Files.readLines(urlFile, Charsets.UTF_8);
            for (String line : lines) {
                checkHttp(line);
            }
        }
    }

    Splitter splitter = Splitter.on(",").trimResults();

    private void checkHttp(String line) {
        if (StringUtils.isEmpty(line)) return;

        // name,url
        List<String> parts = splitter.splitToList(line);
        val name = parts.get(0);
        val url = parts.get(1);

        val result = new HttpReq(url).get();
        if (StringUtils.indexOf(result, name) != INDEX_NOT_FOUND) return;

        msgService.sendMsg("HTTP巡检异常", name);
    }
}
