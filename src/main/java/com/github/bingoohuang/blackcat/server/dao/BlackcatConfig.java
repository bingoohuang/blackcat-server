package com.github.bingoohuang.blackcat.server.dao;

import com.github.bingoohuang.blackcat.server.domain.BlackcatConfigBean;
import com.google.common.collect.ConcurrentHashMultiset;
import lombok.Data;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/18.
 */
@Configuration
public class BlackcatConfig {
    @Autowired ConfigDao configDao;

    @Bean
    public BlackcatConfigBean configMetrics() {
        val bean = new BlackcatConfigBean();
        bean.setConfigMetrics(configDao.queryConfigMetrics());
        bean.setConfigProcesses(configDao.queryConfigProcess());
        // 读取内存/磁盘/负载等的阈值设置
        bean.setConfigThreshold(configDao.queryConfigThreshold());
        bean.setMethodPkgPrefixes(configDao.queryMethodPackagePrefixTopn());
        bean.setConfigHostnames(configDao.queryHostnames()); // 配置监控的主机列表
        bean.setTimes(ConcurrentHashMultiset.<String>create());
        bean.setBeats(new ConcurrentHashMap<String, Long>());
        return bean;
    }

    @Data
    public static class ConfigMetric {
        private String name;
        private long divbase;
    }

    @Data
    public static class ConfigProcess {
        String processName;
        List<String> hostnames;
        List<String> argKeys;
    }

    @Data
    public static class ConfigThreshold {
        int availMemoryLowRatio;
        long availMemoryLowSize;
        int loadOverRatio;
        int availDiskLowRatio;
        long availDiskLowSize;
        long methodMaxMillis;
    }
}
