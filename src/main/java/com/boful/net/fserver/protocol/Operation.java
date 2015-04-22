package com.boful.net.fserver.protocol;

public class Operation {
	/**
	 * 上传文件
	 */
	public static final int TAG_SEND = 0xEEBB01;
	public static final int TAG_DOWNLOAD = 0xEEBB02;
	public static final int TAG_SEND_STATE = 0xEEBB03;
	public static final int TAG_STATE_SEND_OK = 0xEEBB11;
}
