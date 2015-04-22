package com.boful.net.fserver.protocol;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.mina.core.buffer.IoBuffer;

public class SendStateProtocol {
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
		ioBuffer.putInt(OPERATION);
		ioBuffer.putInt(state);
		ioBuffer.putInt(srcPath.getBytes("UTF-8").length);
		ioBuffer.putInt(destPath.getBytes("UTF-8").length);
		byte[] hashBuffer;
		try {
			hashBuffer = Hex.decodeHex(getHash().toCharArray());
		} catch (DecoderException e) {
			throw new IOException(e.getMessage());
		}
		ioBuffer.put(hashBuffer);
		ioBuffer.put(srcPath.getBytes("UTF-8"));
		ioBuffer.put(destPath.getBytes("UTF-8"));
		return ioBuffer;
	}

	// 解码
	public static SendStateProtocol parse(IoBuffer ioBuffer) throws IOException {
		if (ioBuffer.remaining() < 32) {
			return null;
		}

		int state = ioBuffer.getInt();
		int srcLen = ioBuffer.getInt();
		int destLen = ioBuffer.getInt();
		byte[] hashBuffer = new byte[16];
		ioBuffer.get(hashBuffer);
		String hash = Hex.encodeHexString(hashBuffer).toUpperCase();

		if (ioBuffer.remaining() < (srcLen + destLen)) {
			return null;
		}
		byte[] srcPathBuffer = new byte[srcLen];
		byte[] destPathBuffer = new byte[destLen];
		ioBuffer.get(srcPathBuffer);
		ioBuffer.get(destPathBuffer);
		String srcPath = new String(srcPathBuffer, "UTF-8");
		String destPath = new String(destPathBuffer, "UTF-8");

		SendStateProtocol sendStateProtocol = new SendStateProtocol();
		sendStateProtocol.setDestFile(new File(destPath));
		sendStateProtocol.setSrcFile(new File(srcPath));
		sendStateProtocol.setHash(hash);
		sendStateProtocol.setState(state);

		return sendStateProtocol;
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
