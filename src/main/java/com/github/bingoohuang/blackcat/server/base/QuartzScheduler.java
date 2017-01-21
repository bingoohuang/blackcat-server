package com.github.bingoohuang.blackcat.server.base;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component @Slf4j
public class QuartzScheduler {
    Scheduler scheduler;

    {
        init();
    }

    @SneakyThrows
    private void init() {
        Properties p = new Properties();
        p.put("org.quartz.scheduler.skipUpdateCheck", "true");
        p.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        p.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        p.put("org.quartz.threadPool.threadCount", "1");

        val schedFact = new StdSchedulerFactory(p);
        scheduler = schedFact.getScheduler();
        scheduler.start();
    }

    public void scheduleJob(JobDetail jobDetail, Trigger trigger) {
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error("schedule job failed", e);
        }
    }


    public void deleteJob(JobKey key) {
        try {
            scheduler.deleteJob(key);
        } catch (SchedulerException e) {
            log.error("delete job failed", e);
        }
    }
}
