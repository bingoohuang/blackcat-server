package com.github.bingoohuang.blackcat.server.domain;

import com.github.bingoohuang.blackcat.server.base.BlackcatEventReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatMemory;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReqHead;
import lombok.Getter;

@Getter
public class BlackcatMemoryReq implements BlackcatEventReq {
    private final String hostname;
    private final long timestamp;
    private final BlackcatMemory memory;

    public BlackcatMemoryReq(BlackcatReqHead head, BlackcatMemory memory) {
        this.hostname = head.getHostname();
        this.timestamp = head.getTimestamp();
        this.memory = memory;
    }

    @Override
    public boolean isFromBlackcatAgent() {
        return true;
    }
}
