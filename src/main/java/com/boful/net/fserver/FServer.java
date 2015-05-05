package com.boful.net.fserver;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.boful.net.fserver.codec.BofulCodec;
import com.boful.net.fserver.utils.ConfigUtils;

public class FServer {
    /***
     * 解码器定义
     */
    private static BofulCodec bofulCodec = new BofulCodec();
    /***
     * 服务器端业务处理
     */
    private static ServerHandler serverHandler = new ServerHandler();

    private static NioSocketAcceptor acceptor = new NioSocketAcceptor();
    private static Logger logger = Logger.getLogger(FServer.class);

    public static void startServer() {
        logger.debug("服务器开始启动...........");
        int[] config = ConfigUtils.initServerConfig();
        try {
            acceptor.getFilterChain().addLast("logger", new LoggingFilter());
            acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(bofulCodec));
            acceptor.setHandler(serverHandler);
            acceptor.getSessionConfig().setReadBufferSize(config[0]);
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, config[1]);
            acceptor.bind(new InetSocketAddress(config[2]));
        } catch (IOException e) {
            logger.debug("服务器启动失败...........");
            logger.debug("错误信息：" + e.getMessage());
            System.exit(0);
        }
        logger.debug("服务器启动成功...........");
        System.out.println("fserver服务器启动成功...........");
    }

    public static void main(String[] args) throws IOException {
        startServer();
    }
}
