package com.github.bingoohuang.blackcat.server.domain;

import com.github.bingoohuang.blackcat.server.base.BlackcatEventReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatLoad;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReqHead;
import lombok.Getter;

import java.util.List;

@Getter
public class BlackcatLoadReq implements BlackcatEventReq {
    private final long timestamp;
    private final String hostname;
    private final BlackcatLoad load;
    private final List<BlackcatLoad.TopProcess> topProcessList;

    public BlackcatLoadReq(BlackcatReqHead head, BlackcatLoad load) {
        this.hostname = head.getHostname();
        this.timestamp = head.getTimestamp();
        this.topProcessList = load.getTopProcessList();

        this.load = load;
    }

    @Override
    public boolean isFromBlackcatAgent() {
        return true;
    }
}
