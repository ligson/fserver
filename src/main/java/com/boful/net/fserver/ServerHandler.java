package com.boful.net.fserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.mortbay.log.Log;

import com.boful.common.file.utils.FileUtils;
import com.boful.net.fserver.protocol.DownloadProtocol;
import com.boful.net.fserver.protocol.Operation;
import com.boful.net.fserver.protocol.TransferProtocol;

public class ServerHandler extends IoHandlerAdapter {
	private Set<IoSession> sessions = new HashSet<IoSession>();
	private static Logger logger = Logger.getLogger(ServerHandler.class);

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
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		cause.printStackTrace();
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
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
				doReceive(session, transferProtocol);
			}
			if(operation==Operation.TAG_DOWNLOAD){
				DownloadProtocol downloadProtocol=(DownloadProtocol) message;
				doDownLoad(session,downloadProtocol);
			}
		}
	}
	
	private void doDownLoad(IoSession session, DownloadProtocol downloadProtocol){
		File dest=downloadProtocol.getDest();
		File src=downloadProtocol.getSrc();
		try {
			if(src.exists()){
				InputStream inputStream = new FileInputStream(src);
				int bufferSize = 64 * 1024;
				byte[] buffer = new byte[bufferSize];
				int len = -1;
				long offset = 0;
				String fileHash = FileUtils.getHexHash(src);
				while ((len = inputStream.read(buffer)) > 0) {
					TransferProtocol transferProtocol = new TransferProtocol();
					transferProtocol.setSrcFile(src);
					transferProtocol.setDestFile(dest);
					transferProtocol.setFileSize(src.length());
					transferProtocol.setLen(len);
					transferProtocol.setHash(fileHash);
					transferProtocol.setOffset(offset);
					transferProtocol.setBuffer(buffer);
					session.write(transferProtocol);
					offset += bufferSize;
				}
				inputStream.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void doReceive(IoSession session, TransferProtocol transferProtocol) {
		File dest = transferProtocol.getDestFile();
		double process = transferProtocol.getOffset() * 1.00
				/ transferProtocol.getFileSize();
		logger.debug("src:" + transferProtocol.getSrcFile().getAbsolutePath()
				+ "-dest:" + transferProtocol.getDestFile().getAbsolutePath()
				+ "-接收进度:" + process * 100 + "%");
		try {
			if (!dest.exists()) {
				dest.getParentFile().mkdirs();
				dest.createNewFile();
			}
			String writerKey = dest.getAbsolutePath() + "_writer";
			Object object = session.getAttribute(writerKey);
			RandomAccessFile randomAccessFile = null;
			if (object != null) {
				randomAccessFile = (RandomAccessFile) object;
			} else {
				randomAccessFile = new RandomAccessFile(dest, "rw");
				session.setAttribute(writerKey, randomAccessFile);
			}

			randomAccessFile.seek(transferProtocol.getOffset());
			randomAccessFile.write(transferProtocol.getBuffer(), 0,
					transferProtocol.getLen());

			if (dest.length() == transferProtocol.getFileSize()) {
				String fileHash = FileUtils.getHexHash(dest);
				String srcHash = transferProtocol.getHash();
				logger.info("src:"
						+ transferProtocol.getSrcFile().getAbsolutePath()
						+ "-dest:" + dest.getAbsolutePath()
						+ " 传输完成......................hash 是否一致："
						+ fileHash.equals(srcHash));
				randomAccessFile.close();
				session.removeAttribute(writerKey);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
