package com.github.bingoohuang.blackcat.server.domain;

import com.github.bingoohuang.blackcat.server.base.BlackcatEventReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReqHead;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatTrace;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data @NoArgsConstructor
public class BlackcatTraceReq implements BlackcatEventReq {
    private String msg;
    private String msgType;
    private String hostname;
    private long ts;
    private String tsPretty;
    private String linkId;
    private String traceId;

    public BlackcatTraceReq(BlackcatReqHead head, BlackcatTrace trace) {
        traceId = trace.getTraceId();
        linkId = trace.getLinkId();
        ts = head.getTimestamp();
        tsPretty = new DateTime(ts).toString("yyyy-MM-dd HH:mm:ss.SSS");
        hostname = head.getHostname();
        msgType = trace.getMsgType();
        msg = trace.getMsg();
    }

    @Override
    public boolean isFromBlackcatAgent() {
        return false;
    }
}
