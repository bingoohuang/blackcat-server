package com.github.bingoohuang.blackcat.server.dao;

import com.github.bingoohuang.cqler.annotations.Cql;
import com.github.bingoohuang.cqler.annotations.Cqler;

import java.util.List;

@Cqler(keyspace = "blackcat")
public interface ConfigDao {
    @Cql("SELECT processName, hostnames, argKeys FROM config_process")
    List<BlackcatConfig.ConfigProcess> queryConfigProcess();

    @Cql("SELECT availMemoryLowRatio, availMemoryLowSize, " +
            "loadOverRatio, " +
            "availDiskLowRatio, availDiskLowSize, " +
            "methodMaxMillis " +
            "FROM config_threshold")
    BlackcatConfig.ConfigThreshold queryConfigThreshold();

    @Cql("SELECT hostname FROM config_hostname")
    List<String> queryHostnames();

    @Cql("SELECT methodPackagePrefix FROM config_method_topn")
    List<String> queryMethodPackagePrefixTopn();

    @Cql("SELECT name, divbase FROM config_metric")
    List<BlackcatConfig.ConfigMetric> queryConfigMetrics();
}
