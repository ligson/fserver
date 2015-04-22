package com.boful.net.fserver.protocol;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.mina.core.buffer.IoBuffer;

public class DownloadProtocol {
	public static final int OPERATION = Operation.TAG_DOWNLOAD;
	private File src;
	private File dest;

	public File getSrc() {
		return src;
	}

	public void setSrc(File src) {
		this.src = src;
	}

	public File getDest() {
		return dest;
	}

	public void setDest(File dest) {
		this.dest = dest;
	}

	public static int getOperation() {
		return OPERATION;
	}

	// 编码
	public IoBuffer toByteArray() throws IOException {
		String srcPath = src.getAbsolutePath();
		String destPath = dest.getAbsolutePath();
		byte[] srcPathBuffer = srcPath.getBytes("UTF-8");
		byte[] destPathBuffer = destPath.getBytes("UTF-8");
		int count = countLength();

		IoBuffer ioBuffer = IoBuffer.allocate(count);
		ioBuffer.putInt(OPERATION);
		ioBuffer.putInt(srcPathBuffer.length);
		ioBuffer.putInt(destPathBuffer.length);
		ioBuffer.put(srcPathBuffer);
		ioBuffer.put(destPathBuffer);
		return ioBuffer;
	}

	// 解码
	public static DownloadProtocol parse(IoBuffer ioBuffer) throws IOException {
		if (ioBuffer.remaining() < 8) {
			return null;
		}

		int srcPathBufferLen = ioBuffer.getInt();
		int destPathBufferLen = ioBuffer.getInt();
		if (ioBuffer.remaining() < (srcPathBufferLen + destPathBufferLen)) {
			return null;
		}

		byte[] srcPathBuffer = new byte[srcPathBufferLen];
		byte[] destPathBuffer = new byte[destPathBufferLen];
		ioBuffer.get(srcPathBuffer);
		ioBuffer.get(destPathBuffer);

		String srcPath = new String(srcPathBuffer, "UTF-8");
		String destPath = new String(destPathBuffer, "UTF-8");

		DownloadProtocol downloadProtocol = new DownloadProtocol();
		downloadProtocol.setDest(new File(destPath));
		downloadProtocol.setSrc(new File(srcPath));

		return downloadProtocol;
	}

	public int countLength() {
		// TAG+SRCLEN+DESTLEN+srcBuffer + destBuffer
		String srcPath = src.getAbsolutePath();
		String destPath = dest.getAbsolutePath();
		try {
			return 4 + 4 + 4 + srcPath.getBytes("UTF-8").length
					+ destPath.getBytes("UTF-8").length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return -1;
	}

}
