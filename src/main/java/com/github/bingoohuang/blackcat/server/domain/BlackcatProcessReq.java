package com.github.bingoohuang.blackcat.server.domain;

import com.github.bingoohuang.blackcat.server.base.BlackcatEventReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatProcess;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReqHead;
import com.google.common.base.Function;
import lombok.Getter;
import lombok.val;

import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.transform;

@Getter
public class BlackcatProcessReq implements BlackcatEventReq {
    private final String hostname;
    private final long timestamp;
    private final List<Long> pids;
    private final List<String> args;
    private final List<String> names;
    private final List<Long> reses;
    private final List<Date> startTimes;

    public BlackcatProcessReq(BlackcatReqHead head, BlackcatProcess process) {
        hostname = head.getHostname();
        timestamp = head.getTimestamp();

        val procs = process.getProcList();
        pids = transform(procs, new Function<BlackcatProcess.Proc, Long>() {
            @Override public Long apply(BlackcatProcess.Proc p) {
                return p.getPid();
            }
        });
        args = transform(procs, new Function<BlackcatProcess.Proc, String>() {
            @Override public String apply(BlackcatProcess.Proc p) {
                return p.getArgs();
            }
        });
        names = transform(procs, new Function<BlackcatProcess.Proc, String>() {
            @Override public String apply(BlackcatProcess.Proc p) {
                return p.getName();
            }
        });
        reses = transform(procs, new Function<BlackcatProcess.Proc, Long>() {
            @Override public Long apply(BlackcatProcess.Proc p) {
                return p.getRes();
            }
        });
        startTimes = transform(procs, new Function<BlackcatProcess.Proc, Date>() {
            @Override public Date apply(BlackcatProcess.Proc p) {
                return new Date(p.getStartTime());
            }
        });
    }

    @Override
    public boolean isFromBlackcatAgent() {
        return true;
    }
}
