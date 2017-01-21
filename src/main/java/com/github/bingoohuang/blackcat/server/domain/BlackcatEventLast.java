package com.github.bingoohuang.blackcat.server.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class BlackcatEventLast {
    String hostname;
    String eventType;
    long lastTs;
}
