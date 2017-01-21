package com.github.bingoohuang.blackcat.server.job;

import com.github.bingoohuang.blackcat.server.base.BlackcatJob;
import com.github.bingoohuang.blackcat.server.base.MsgService;
import com.github.bingoohuang.blackcat.server.base.QuartzScheduler;
import com.google.common.collect.ConcurrentHashMultiset;
import lombok.SneakyThrows;
import lombok.val;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public abstract class AbstractMsgJob implements BlackcatJob {
    @Autowired MsgService msgService;
    @Autowired @Qualifier("times") ConcurrentHashMultiset<String> times;
    @Autowired @Qualifier("configHostnames") List<String> configHostnames;

    @Override
    public void scheduleJob(QuartzScheduler scheduler) {
        val job = createMsgJob();
        val trigger = createMsgTrigger();
        scheduler.scheduleJob(job, trigger);
    }

    private JobDetail createMsgJob() {
        val job = newJob(MsgQuartzJob.class).build();
        job.getJobDataMap().put("handler", this);
        return job;
    }

    public static class MsgQuartzJob implements Job {
        @Override @SneakyThrows
        public void execute(JobExecutionContext context) {
            val jobDataMap = context.getJobDetail().getJobDataMap();
            val handler = (AbstractMsgJob) jobDataMap.get("handler");

            handler.triggerMsg();
        }
    }


    private Trigger createMsgTrigger() {
        val cron = getCron();
        return newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
    }

    protected abstract String getCron();

    private void triggerMsg() {
        val alertTimesMsg = compositeAlertTimesMsg();

        msgService.sendMsg("最近告警次数", alertTimesMsg);
    }

    static public final String MEMORY_TIMES = "-Memory-times";
    static public final String MEMORY_ALERTS = "-Memory-alerts";
    static public final String FILE_STORES_TIMES = "-FileStores-times";
    static public final String FILE_STORES_ALERTS = "-FileStores-alerts";
    static public final String LOAD_TIMES = "-Load-times";
    static public final String LOAD_ALERTS = "-Load-alerts";
    static public final String PROCESS_TIMES = "-Process-times";
    static public final String PROCESS_ALERTS = "-Process-alerts";
    static public final String METHODRUNTIME_TIMES = "-MethodRuntime-times";
    static public final String METHODRUNTIME_ALERTS = "-MethodRuntime-alerts";

    private String compositeAlertTimesMsg() {
        int lastMemoryTimes = 0, lastMemoryAlerts = 0;
        int lastFileStoresTimes = 0, lastFileStoresAlerts = 0;
        int lastLoadTimes = 0, lastLoadAlerts = 0;
        int lastProcessTimes = 0, lastProcessAlerts = 0;

        for (String hostname : configHostnames) {
            lastMemoryTimes += times.count(hostname + MEMORY_TIMES);
            lastMemoryAlerts += times.count(hostname + MEMORY_ALERTS);

            lastFileStoresTimes += times.count(hostname + FILE_STORES_TIMES);
            lastFileStoresAlerts += times.count(hostname + FILE_STORES_ALERTS);

            lastLoadTimes += times.count(hostname + LOAD_TIMES);
            lastLoadAlerts += times.count(hostname + LOAD_ALERTS);

            lastProcessTimes += times.count(hostname + PROCESS_TIMES);
            lastProcessAlerts += times.count(hostname + PROCESS_ALERTS);
        }

        times.clear();

        return new StringBuilder("\n")
                .append("内存").append(lastMemoryAlerts)
                .append('/').append(lastMemoryTimes).append("次\n")
                .append("磁盘").append(lastFileStoresAlerts)
                .append('/').append(lastFileStoresTimes).append("次\n")
                .append("负载").append(lastLoadAlerts)
                .append('/').append(lastLoadTimes).append("次\n")
                .append("进程").append(lastProcessAlerts)
                .append('/').append(lastProcessTimes).append("次\n")
                .toString();
    }
}
