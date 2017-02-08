package com.github.bingoohuang.blackcat.server.domain;

import com.github.bingoohuang.blackcat.server.dao.BlackcatConfig;
import com.google.common.collect.Multiset;
import lombok.Data;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/7.
 */
@Data
public class BlackcatConfigBean {
    private List<BlackcatConfig.ConfigMetric> configMetrics;
    private List<BlackcatConfig.ConfigProcess> configProcesses;
    private BlackcatConfig.ConfigThreshold configThreshold;
    private List<String> methodPkgPrefixes;
    private List<String> configHostnames;
    private Multiset<String> times;
    private ConcurrentMap<String, Long> beats;
}
