package com.boful.net.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.boful.net.client.event.TransferEvent;
import com.boful.net.fserver.HandlerUtil;
import com.boful.net.fserver.protocol.Operation;
import com.boful.net.fserver.protocol.SendStateProtocol;
import com.boful.net.fserver.protocol.TransferProtocol;

public class FServerClientHandler extends IoHandlerAdapter {

    private Set<IoSession> sessions = new HashSet<IoSession>();
    private static Logger logger = Logger.getLogger(FServerClientHandler.class);
    private TransferEvent transferEvent;

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        sessions.add(session);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        Field field = null;
        try {
            field = message.getClass().getDeclaredField("OPERATION");
            field.setAccessible(true);
        } catch (NoSuchFieldException exception) {
            logger.debug(exception);
        }
        if (field != null) {
            int operation = field.getInt(message);
            if (operation == Operation.TAG_SEND_STATE) {
                SendStateProtocol sendStateProtocol = (SendStateProtocol) message;
                // 文件传输成功
                if (sendStateProtocol.getState() == Operation.TAG_STATE_SEND_OK) {
                    transferEvent.onSuccess(sendStateProtocol.getSrcFile(), sendStateProtocol.getDestFile());
                    logger.info("文件" + sendStateProtocol.getSrcFile().getAbsolutePath() + "传输成功！");
                } else {
                    logger.info("文件" + sendStateProtocol.getSrcFile().getAbsolutePath() + "传输失败！");
                    transferEvent.onFail(sendStateProtocol.getSrcFile(), sendStateProtocol.getDestFile(), "error");
                }
            } else if (operation == Operation.TAG_SEND) {
                TransferProtocol transferProtocol = (TransferProtocol) message;
                int process = (int) (transferProtocol.getOffset() * 1.00 / transferProtocol.getFileSize()) * 100;
                if (process >= 100) {
                    process = 100;
                }
                transferEvent.onTransfer(transferProtocol.getSrcFile(), transferProtocol.getDestFile(), process);
                HandlerUtil.doReceive(session, transferProtocol);
                if (process == 100) {
                    transferEvent.onSuccess(transferProtocol.getSrcFile(), transferProtocol.getDestFile());
                }
            } else if (operation == Operation.TAG_SEND_DOWNLOAD) {
                TransferProtocol transferProtocol = (TransferProtocol) message;
                File dest = new File(transferProtocol.getDestFile());
                OutputStream output = new FileOutputStream(dest, true);
                output.write(transferProtocol.getBuffer());
                output.close();
            }
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    public void setTransferEvent(TransferEvent transferEvent) {
        this.transferEvent = transferEvent;
    }

}
