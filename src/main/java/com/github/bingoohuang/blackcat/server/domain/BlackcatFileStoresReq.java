package com.github.bingoohuang.blackcat.server.domain;

import com.github.bingoohuang.blackcat.server.base.BlackcatEventReq;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatFileStores;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatFileStores.FileStore;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReqHead;
import com.google.common.base.Function;
import lombok.Getter;
import lombok.val;

import java.util.List;

import static com.google.common.collect.Lists.transform;

@Getter
public class BlackcatFileStoresReq implements BlackcatEventReq {
    private final String hostname;
    private final long timestamp;
    private final List<String> names;
    private final List<String> descriptions;
    private final List<Long> totals;
    private final List<Long> usables;

    public BlackcatFileStoresReq(BlackcatReqHead head, BlackcatFileStores stores) {
        hostname = head.getHostname();
        timestamp = head.getTimestamp();

        val fileStores = stores.getFileStoreList();
        names = transform(fileStores, new Function<FileStore, String>() {
            @Override
            public String apply(FileStore f) {
                return f.getName();
            }
        });
        descriptions = transform(fileStores, new Function<FileStore, String>() {
            @Override
            public String apply(FileStore f) {
                return f.getDescription();
            }
        });
        totals = transform(fileStores, new Function<FileStore, Long>() {
            @Override
            public Long apply(FileStore f) {
                return f.getTotal();
            }
        });
        usables = transform(fileStores, new Function<FileStore, Long>() {
            @Override
            public Long apply(FileStore f) {
                return f.getUsable();
            }
        });
    }

    @Override
    public boolean isFromBlackcatAgent() {
        return true;
    }
}
