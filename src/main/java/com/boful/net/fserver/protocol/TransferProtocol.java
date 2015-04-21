package com.boful.net.fserver.protocol;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.mina.core.buffer.IoBuffer;

public class TransferProtocol {
	public static final int OPERATION = Operation.TAG_SEND;
	private File destFile;
	private File srcFile;

	private long fileSize;
	private long offset;
	private int len;
	private byte[] buffer;

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

	public IoBuffer toByteArray() throws IOException {
		String srcPath = srcFile.getAbsolutePath();
		String destPath = destFile.getAbsolutePath();

		IoBuffer ioBuffer = IoBuffer.allocate(countLength());
		ioBuffer.putInt(OPERATION);
		ioBuffer.putInt(srcPath.getBytes("UTF-8").length);
		ioBuffer.putInt(destPath.getBytes("UTF-8").length);
		ioBuffer.putLong(srcFile.length());
		ioBuffer.putLong(offset);
		ioBuffer.putInt(len);
		ioBuffer.put(srcPath.getBytes("UTF-8"));
		ioBuffer.put(destPath.getBytes("UTF-8"));
		ioBuffer.put(buffer);
		return ioBuffer;
	}

	public static TransferProtocol parse(IoBuffer ioBuffer) throws IOException {
		if (ioBuffer.remaining() < 36) {
			return null;
		}
		TransferProtocol transferProtocol = new TransferProtocol();
		int srcPathLen = ioBuffer.getInt();
		int destPathLen = ioBuffer.getInt();
		long fileLength = ioBuffer.getLong();
		transferProtocol.setFileSize(fileLength);
		transferProtocol.setOffset(ioBuffer.getLong());
		transferProtocol.setLen(ioBuffer.getInt());

		int remainLen = srcPathLen + destPathLen + transferProtocol.getLen();
		if (ioBuffer.remaining() < remainLen) {
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
			return 4 + 4 * 2 + 8 * 2 + 4 + srcPath.getBytes("UTF-8").length
					+ destPath.getBytes("UTF-8").length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
