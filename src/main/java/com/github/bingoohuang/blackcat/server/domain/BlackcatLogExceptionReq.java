package com.github.bingoohuang.blackcat.server.domain;

import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatLogException;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReqHead;
import com.github.bingoohuang.blackcat.server.base.BlackcatEventReq;
import com.github.bingoohuang.westid.WestId;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor
public class BlackcatLogExceptionReq implements BlackcatEventReq {
    private String logId;
    private String hostname;
    private String logger;
    private String tcode;
    private String tid;
    private String exceptionNames;
    private String contextLogs;
    private String timestamp;

    public BlackcatLogExceptionReq(BlackcatReqHead head, BlackcatLogException logException) {
        this.logId = String.valueOf(WestId.next());
        this.hostname = head.getHostname();
        this.logger = logException.getLogger();
        this.tcode = logException.getTcode();
        this.tid = logException.getTid();
        this.exceptionNames = logException.getExceptionNames();
        this.contextLogs = logException.getContextLogs();
        this.timestamp = logException.getTimestamp();
    }

    @Override public boolean isFromBlackcatAgent() {
        return true;
    }
}
