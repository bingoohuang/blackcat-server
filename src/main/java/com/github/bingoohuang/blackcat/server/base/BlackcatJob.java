package com.github.bingoohuang.blackcat.server.base;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/18.
 */
public interface BlackcatJob {
    void scheduleJob(QuartzScheduler scheduler);
}
