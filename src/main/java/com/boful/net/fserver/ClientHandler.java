package com.boful.net.fserver;

import java.util.HashSet;
import java.util.Set;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.log4j.Logger;

public class ClientHandler extends IoHandlerAdapter {

	private Set<IoSession> sessions = new HashSet<IoSession>();
	private static Logger logger = Logger.getLogger(ClientHandler.class);

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		sessions.remove(session);
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		sessions.add(session);

	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		super.messageReceived(session, message);
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		super.messageSent(session, message);
	}

}
