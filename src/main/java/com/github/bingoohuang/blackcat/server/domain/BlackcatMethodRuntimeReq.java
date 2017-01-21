package com.github.bingoohuang.blackcat.server.domain;

import com.github.bingoohuang.blackcat.server.base.BlackcatEventReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatMethodRuntime;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReqHead;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public class BlackcatMethodRuntimeReq implements BlackcatEventReq {
    public final String hostname;
    public final long timestamp;
    public final BlackcatMethodRuntime rt;

    public BlackcatMethodRuntimeReq(BlackcatReqHead head, BlackcatMethodRuntime rt) {
        this.hostname = head.getHostname();
        this.timestamp = head.getTimestamp();
        this.rt = rt;
    }

    @Override
    public boolean isFromBlackcatAgent() {
        return false;
    }
}
