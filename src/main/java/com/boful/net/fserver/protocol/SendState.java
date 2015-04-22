package com.boful.net.fserver.protocol;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.mina.core.buffer.IoBuffer;

public class SendState {
	public static final int OPERATION = Operation.TAG_SEND_STATE;
	private int state = Operation.TAG_STATE_SEND_OK;
	private File srcFile;
	private File destFile;
	private String hash;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public File getSrcFile() {
		return srcFile;
	}

	public void setSrcFile(File srcFile) {
		this.srcFile = srcFile;
	}

	public File getDestFile() {
		return destFile;
	}

	public void setDestFile(File destFile) {
		this.destFile = destFile;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public static int getOperation() {
		return OPERATION;
	}

	// 编码
	public IoBuffer toByteArray() throws IOException {
		String srcPath = srcFile.getAbsolutePath();
		String destPath = destFile.getAbsolutePath();

		IoBuffer ioBuffer = IoBuffer.allocate(countLength());
		
		return ioBuffer;
	}

	// 解码
	public static TransferProtocol parse(IoBuffer ioBuffer) throws IOException {
		if (ioBuffer.remaining() < 32) {
			return null;
		}
		TransferProtocol transferProtocol = new TransferProtocol();
		int srcPathLen = ioBuffer.getInt();
		int destPathLen = ioBuffer.getInt();
		long fileLength = ioBuffer.getLong();
		transferProtocol.setFileSize(fileLength);
		transferProtocol.setOffset(ioBuffer.getLong());
		transferProtocol.setLen(ioBuffer.getInt());
		// read hash
		if (ioBuffer.remaining() < 16) {
			return null;
		}
		byte[] hashBuffer = new byte[16];
		ioBuffer.get(hashBuffer);
		transferProtocol.setHash(Hex.encodeHexString(hashBuffer));

		int remainLen = srcPathLen + destPathLen + transferProtocol.getLen();
		System.out.println(remainLen + ">>>>>>>>>>>>>>>>>>>>");
		int remain = ioBuffer.remaining();
		System.out.println(remain);
		if (remain < remainLen) {
			return null;
		}
		byte[] srcBuffer = new byte[srcPathLen];
		byte[] destBuffer = new byte[destPathLen];
		byte[] buffer = new byte[transferProtocol.getLen()];
		ioBuffer.get(srcBuffer);
		ioBuffer.get(destBuffer);
		ioBuffer.get(buffer);

		transferProtocol.setSrcFile(new File(new String(srcBuffer, "UTF-8")));
		transferProtocol.setDestFile(new File(new String(destBuffer, "UTF-8")));
		transferProtocol.setBuffer(buffer);
		return transferProtocol;
	}

	public int countLength() {
		// TAG+STATE+SRCLEN+DESTLEN+HASH+srcBuffer+destBuffer
		String srcPath = srcFile.getAbsolutePath();
		String destPath = destFile.getAbsolutePath();

		try {
			return 4 + 4 + 4 + 4 + 16 + srcPath.getBytes("UTF-8").length
					+ destPath.getBytes("UTF-8").length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return -1;
	}

}
