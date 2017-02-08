-- noinspection SqlNoDataSourceInspectionForFile

-- noinspection SqlDialectInspectionForFile

-- https://cassandra.apache.org/doc/cql3/CQL.html

CREATE KEYSPACE blackcat with replication =
    {'class': 'SimpleStrategy', 'replication_factor': 1 };

USE blackcat;

DROP TABLE IF EXISTS event_memory;
CREATE TABLE event_memory(
   hostname text,
   ts bigint,
   total bigint,
   available bigint,
   PRIMARY KEY ((hostname), ts)
) WITH default_time_to_live = 2592000;
-- The default expiration time in seconds for a table.
-- 30days 2592000 = 30*24*60*60

DROP TABLE IF EXISTS event_last;
CREATE TABLE event_last(
   hostname text,
   event_type text,
   last_ts bigint,
   PRIMARY KEY (hostname, event_type)
);


DROP TABLE IF EXISTS event_stores;
CREATE TABLE event_stores(
   hostname text,
   ts bigint,
   names list<text>,
   descriptions list<text>,
   totals list<bigint>,
   usables list<bigint>,
   PRIMARY KEY ((hostname), ts)
) WITH default_time_to_live = 2592000;

DROP TABLE IF EXISTS event_load;
CREATE TABLE event_load(
   hostname text,
   ts bigint,
   cpuNum int,
   oneMinAvg float,
   fiveMinsAvg float,
   fifteenMinsAvg float,
   PRIMARY KEY ((hostname), ts)
) WITH default_time_to_live = 2592000;

DROP TABLE IF EXISTS event_process;
CREATE TABLE event_process(
   hostname text,
   ts bigint,
   args list<text>,
   pids list<bigint>,
   reses list<bigint>,
   startTimes list<timestamp>,
   names list<text>,
   PRIMARY KEY ((hostname), ts)
) WITH default_time_to_live = 2592000;

DROP TABLE IF EXISTS event_method_runtime;
CREATE TABLE event_method_runtime(
   invokeId text,
   traceId text,
   linkId text,
   className text,
   methodName text,
   methodDesc text,
   startMillis bigint,
   endMillis bigint,
   costNano bigint,
   args text,
   pid text,
   executionId text,
   result text,
   throwableCaught text,
   throwableUncaught text,
   throwableMessage text,
   sameThrowable boolean,
   hostname text,
   ts bigint,
   PRIMARY KEY (invokeId)
) WITH default_time_to_live = 2592000;

DROP TABLE IF EXISTS config_hostname;
CREATE TABLE config_hostname (
    hostname text,
    PRIMARY KEY ((hostname))
);

DROP TABLE IF EXISTS event_trace;
CREATE TABLE event_trace(
   traceId text,
   linkId text,
   ts bigint,
   tsPretty text,
   hostname text,
   msgType text,
   msg text,
   PRIMARY KEY ((traceId), linkId)
) WITH default_time_to_live = 2592000;

DROP TABLE IF EXISTS config_method_topn;
CREATE TABLE config_method_topn (
    seq int,
    methodPackagePrefix text,
    PRIMARY KEY (seq)
);

INSERT INTO config_method_topn(seq, methodPackagePrefix)
VALUES (1, 'com.github.bingoohuang.blackcat');

DROP TABLE IF EXISTS config_process;
CREATE TABLE config_process (
    processName text,
    hostnames list<text>,
    argKeys list<text>,
    configTime timestamp,
    PRIMARY KEY ((processName))
);

INSERT INTO config_process(processName, hostnames, argKeys, configTime)
   VALUES('yoga-admin', ['hb2-hi-app01','hb2-hi-app02'], ['yoga-admin'],'now');
INSERT INTO config_process(processName, hostnames, argKeys, configTime)
   VALUES('yoga-system', ['hb2-hi-app01','hb2-hi-app02'], ['yoga-system'],'now');


DROP TABLE IF EXISTS config_threshold;
CREATE TABLE config_threshold (
    configKey text,
    availMemoryLowRatio int,
    availMemoryLowSize bigint,
    loadOverRatio int,
    availDiskLowRatio int,
    availDiskLowSize bigint,
    methodMaxMillis bigint,
    PRIMARY KEY (configKey)
);

INSERT INTO config_threshold(configKey,
    availMemoryLowRatio, availMemoryLowSize,
    loadOverRatio,
    availDiskLowRatio, availDiskLowSize,
    methodMaxMillis)
VALUES('threshold',
    10, 209715200,
    10,
    10, 1073741824,
    3000
    );

-- 指标采集表
DROP TABLE IF EXISTS event_metric;
CREATE TABLE event_metric (
    name text, -- 指标名称,建议形式为:scope1.scope2.scope3
    day text, -- eg. 20160102
    seq bigint,
    val counter,
    PRIMARY KEY((name, day), seq)
);


-- 指标划分配置表
-- 如果指标没有划分配置,默认为小时,即划分为60*60
DROP TABLE IF EXISTS config_metric;
CREATE TABLE config_metric (
    name text,
    detail text, -- 指标明说
    divbase bigint, -- eg. 以秒为单位,按小时则为60*60,按分钟则为60,按10分钟则为600
    PRIMARY KEY(name)
);
