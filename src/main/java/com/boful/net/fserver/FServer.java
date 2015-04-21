package com.boful.net.fserver;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.boful.net.fserver.codec.BofulCodec;

import org.apache.log4j.Logger;

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

	public static void main(String[] args) throws IOException {
		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		acceptor.getFilterChain().addLast("codec",
				new ProtocolCodecFilter(bofulCodec));
		acceptor.setHandler(serverHandler);

		acceptor.getSessionConfig().setReadBufferSize(2048);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
		acceptor.bind(new InetSocketAddress(8888));
		logger.debug("starting...........");
	}
}
