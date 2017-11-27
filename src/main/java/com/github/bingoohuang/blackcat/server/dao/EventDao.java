package com.github.bingoohuang.blackcat.server.dao;

import com.github.bingoohuang.blackcat.server.domain.*;
import com.github.bingoohuang.cqler.annotations.Cql;
import com.github.bingoohuang.cqler.annotations.Cqler;

@Cqler(keyspace = "blackcat")
public interface EventDao {

    @Cql("INSERT INTO event_log_exception(logId, logger, tcode, tid, exceptionNames, contextLogs, timestamp) " +
            "VALUES (#logId#, #logger#, #tcode#, #tid#, #exceptionNames#, #contextLogs#, #timestamp#)")
    void addLogException(BlackcatLogExceptionReq logException);

    @Cql("INSERT INTO event_memory(hostname, ts, total, available) " +
            "VALUES (#hostname#, #timestamp#, #memory.total#, #memory.available#)")
    void addMemoryEvent(BlackcatMemoryReq memory);

    @Cql("INSERT INTO event_stores(hostname,  ts, names, descriptions,   totals,   usables) " +
            "VALUES (#hostname#, #timestamp#, #names#, #descriptions#, #totals#, #usables#)")
    void addFileStoresEvent(BlackcatFileStoresReq stores);

    @Cql("INSERT INTO event_load(hostname, ts, cpuNum, oneMinAvg, fiveMinsAvg, fifteenMinsAvg) " +
            "VALUES (#hostname#, #timestamp#, #load.cpuNum#, #load.oneMinAvg#, #load.fiveMinsAvg#, #load.fifteenMinsAvg#)")
    void addLoadEvent(BlackcatLoadReq load);

    @Cql("INSERT INTO event_process(hostname, ts, args, pids, reses, startTimes, names) " +
            "VALUES (#hostname#, #timestamp#, #args#, #pids#, #reses#, #startTimes#, #names#)")
    void addProcessesEvent(BlackcatProcessReq process);

    @Cql("UPDATE event_last SET last_ts = #lastTs# WHERE hostname = #hostname# AND event_type = #eventType#")
    void updateEventLast(BlackcatEventLast blackcatEventLast);

    @Cql("INSERT INTO event_method_runtime(invokeId, traceId, linkId,   hostname, " +
            "         ts,      className,      methodName,      methodDesc, " +
            "     startMillis,      endMillis,      costNano,      args, " +
            "     pid,      executionId,      result,      throwableCaught," +
            "      throwableUncaught,      sameThrowable, throwableMessage) " +
            "VALUES(                  #rt.invokeId#, #rt.traceId#, #rt.linkId#, #hostname#, " +
            "#timestamp#, #rt.className#, #rt.methodName#, #rt.methodDesc#, " +
            "#rt.startMillis#, #rt.endMillis#, #rt.costNano#, #rt.args#, " +
            "#rt.pid#, #rt.executionId#, #rt.result#, #rt.throwableCaught#, " +
            "#rt.throwableUncaught#, #rt.sameThrowable#, #rt.throwableMessage#)")
    void addMethodRuntime(BlackcatMethodRuntimeReq blackcatMethodRuntime);

    @Cql("INSERT INTO event_trace(traceId, linkId, ts, tsPretty, hostname, msgType, msg) " +
            "VALUES(#traceId#, #linkId#, #ts#, #tsPretty#, #hostname#, #msgType#, #msg#)")
    void updateEventTrace(BlackcatTraceReq blackcatTraceReq);


    @Cql("UPDATE event_metric SET val = val + #val# WHERE name = #name# AND day  = #day# AND seq  = #seq# ")
    void addEventMetric(BlackcatMetricReq blackcatMetricReq);
}
