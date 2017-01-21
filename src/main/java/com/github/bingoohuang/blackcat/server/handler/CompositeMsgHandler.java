package com.github.bingoohuang.blackcat.server.handler;

import com.github.bingoohuang.blackcat.sdk.BlackcatMsgHandler;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReq;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.val;

import java.util.List;

@AllArgsConstructor
public class CompositeMsgHandler implements BlackcatMsgHandler {
    final List<BlackcatMsgHandler> handlers;

    @Override
    public void handle(BlackcatReq req, ChannelHandlerContext ctx) {
        for (val handler : handlers) {
            handler.handle(req, ctx);
        }
    }
}
