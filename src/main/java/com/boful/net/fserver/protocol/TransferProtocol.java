package com.boful.net.fserver.protocol;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.mina.core.buffer.IoBuffer;

public class TransferProtocol {
	public static final int OPERATION = Operation.TAG_SEND;
	private File destFile;
	private File srcFile;

	private long fileSize;
	private long offset;
	private int len;
	private byte[] buffer;
	private String hash;

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public File getDestFile() {
		return destFile;
	}

	public void setDestFile(File destFile) {
		this.destFile = destFile;
	}

	public File getSrcFile() {
		return srcFile;
	}

	public void setSrcFile(File srcFile) {
		this.srcFile = srcFile;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public int getLen() {
		return len;
	}

	public void setLen(int len) {
		this.len = len;
	}

	// 编码
	public IoBuffer toByteArray() throws IOException {
		String srcPath = srcFile.getAbsolutePath();
		String destPath = destFile.getAbsolutePath();
		int count = countLength();

		IoBuffer ioBuffer = IoBuffer.allocate(count);
		ioBuffer.putInt(OPERATION);
		ioBuffer.putInt(srcPath.getBytes("UTF-8").length);
		ioBuffer.putInt(destPath.getBytes("UTF-8").length);
		ioBuffer.putLong(srcFile.length());
		ioBuffer.putLong(offset);
		ioBuffer.putInt(len);
		byte[] hashBuffer;
		try {
			hashBuffer = Hex.decodeHex(getHash().toCharArray());
		} catch (DecoderException e) {
			throw new IOException(e.getMessage());
		}
		System.out.println(hashBuffer.length);
		ioBuffer.put(hashBuffer);
		ioBuffer.put(srcPath.getBytes("UTF-8"));
		ioBuffer.put(destPath.getBytes("UTF-8"));
		ioBuffer.put(buffer);
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
		// TAG+SRCLEN+DESCLEN+START+END+LEN+SRC+DEST;
		String srcPath = srcFile.getAbsolutePath();
		String destPath = destFile.getAbsolutePath();

		try {
			return 4 + 4 * 2 + 8 * 2 + 4 + 16
					+ srcPath.getBytes("UTF-8").length
					+ destPath.getBytes("UTF-8").length + buffer.length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
