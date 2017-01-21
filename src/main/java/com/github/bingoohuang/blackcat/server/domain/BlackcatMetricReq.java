package com.github.bingoohuang.blackcat.server.domain;


import com.github.bingoohuang.blackcat.server.base.BlackcatEventReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatMetric;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReqHead;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data @NoArgsConstructor
public class BlackcatMetricReq implements BlackcatEventReq {
    private long ts;
    private String name;
    private String day;
    private long seq;
    private long val;

    public BlackcatMetricReq(BlackcatReqHead head, BlackcatMetric metric) {
        this.name = metric.getName();
        this.day = new DateTime(head.getTimestamp()).toString("yyyyMMdd");
        this.val = metric.getValue();
        this.ts = head.getTimestamp();
    }

    @Override
    public boolean isFromBlackcatAgent() {
        return false;
    }

}
