package com.github.bingoohuang.blackcat.server.eventlistener;

import com.github.bingoohuang.blackcat.server.base.BlackcatReqListener;
import com.github.bingoohuang.blackcat.server.dao.BlackcatConfig;
import com.github.bingoohuang.blackcat.server.dao.EventDao;
import com.github.bingoohuang.blackcat.server.domain.BlackcatConfigBean;
import com.github.bingoohuang.blackcat.server.domain.BlackcatMetricReq;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Map;

@Component
public class BlackcatMetricReqListener implements BlackcatReqListener {
    @Autowired EventDao eventDao;
    @Autowired BlackcatConfigBean bean;
    Map<String, Long> divbases = Maps.newHashMap();

    @PostConstruct
    public void postConstruct() {
        for (BlackcatConfig.ConfigMetric configMetric : bean.getConfigMetrics()) {
            divbases.put(configMetric.getName(), configMetric.getDivbase());
        }
    }

    public long getDivBase(String metricName) {
        return divbases.containsKey(metricName)
                ? divbases.get(metricName)
                : 60 * 60;
    }

    @Subscribe
    public void deal(BlackcatMetricReq req) {
        long divBase = getDivBase(req.getName());
        // seconds from mid-night

        long millis = req.getTs();
        long millisFromMidnight = getMillisFromMidnight(millis);
        req.setSeq(millisFromMidnight / 1000L / divBase);

        eventDao.addEventMetric(req);
    }

    public static long getMillisFromMidnight(long millis) {
        Calendar m = Calendar.getInstance();
        m.setTimeInMillis(millis); //midnight
        m.set(Calendar.HOUR_OF_DAY, 0);
        m.set(Calendar.MINUTE, 0);
        m.set(Calendar.SECOND, 0);
        m.set(Calendar.MILLISECOND, 0);

        return millis - m.getTimeInMillis();
    }

}
