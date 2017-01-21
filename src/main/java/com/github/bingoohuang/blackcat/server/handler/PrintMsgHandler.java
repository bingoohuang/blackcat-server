package com.github.bingoohuang.blackcat.server.handler;

import com.github.bingoohuang.blackcat.sdk.BlackcatMsgHandler;
import com.github.bingoohuang.blackcat.sdk.protobuf.BlackcatMsg.BlackcatReq;
import io.netty.channel.ChannelHandlerContext;

public class PrintMsgHandler implements BlackcatMsgHandler {
    @Override
    public void handle(BlackcatReq req, ChannelHandlerContext ctx) {
        System.out.println(req);
    }
}
