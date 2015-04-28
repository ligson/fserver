package com.boful.net.fserver;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.boful.net.fserver.protocol.DownloadProtocol;
import com.boful.net.fserver.protocol.Operation;
import com.boful.net.fserver.protocol.TransferProtocol;

public class ServerHandler extends IoHandlerAdapter {
    private Set<IoSession> sessions = new HashSet<IoSession>();
    private static Logger logger = Logger.getLogger(ServerHandler.class);
    private static HandlerUtil handerUtil = new HandlerUtil();

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);
        sessions.add(session);
        System.out.println("connect.................");
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        sessions.remove(session);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        // TODO Auto-generated method stub
        cause.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        super.messageReceived(session, message);
        Field field = null;
        try {
            field = message.getClass().getDeclaredField("OPERATION");
        } catch (NoSuchFieldException exception) {
            logger.debug(exception);
        }
        if (field != null) {
            int operation = field.getInt(message);
            if (operation == Operation.TAG_SEND) {
                TransferProtocol transferProtocol = (TransferProtocol) message;
                handerUtil.doReceive(session, transferProtocol);
            }
            if (operation == Operation.TAG_DOWNLOAD) {
                DownloadProtocol downloadProtocol = (DownloadProtocol) message;
                handerUtil.doDownLoad(session, downloadProtocol);
            }
        }
    }

}
