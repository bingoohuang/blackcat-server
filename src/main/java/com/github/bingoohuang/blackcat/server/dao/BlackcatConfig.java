package com.github.bingoohuang.blackcat.server.dao;

import com.google.common.collect.ConcurrentHashMultiset;
import lombok.Data;
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
    public List<ConfigMetric> configMetrics() {
        return configDao.queryConfigMetrics();
    }

    @Bean
    public List<ConfigProcess> configProcesses() {
        return configDao.queryConfigProcess();
    }

    @Bean // 读取内存/磁盘/负载等的阈值设置
    public ConfigThreshold configThreshold() {
        return configDao.queryConfigThreshold();
    }

    @Bean
    public List<String> methodPkgPrefixes() {
        return configDao.queryMethodPackagePrefixTopn();
    }


    @Bean
    public ConcurrentHashMultiset<String> times() {
        return ConcurrentHashMultiset.create();
    }

    @Bean
    public ConcurrentHashMap<String, Long> beats() {
        return new ConcurrentHashMap<String, Long>();
    }

    @Bean
    public List<String> configHostnames() { // 配置监控的主机列表
        return configDao.queryHostnames();
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
