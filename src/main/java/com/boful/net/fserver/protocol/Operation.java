package com.boful.net.fserver.protocol;

public class Operation {
	/**
	 * 上传文件
	 */
	public static final int TAG_SEND = 0xEEBB01;
	public static final int TAG_DOWNLOAD = 0xEEBB02;
	
	public static final int TAG_SEND_STATE = 0xEEBB03;
	public static final int TAG_STATE_SEND_OK = 0xEEBB11;
	public static final int TAG_STATE_SEND_ERROR=0xEEBB12;
	
	public static final int TAG_DOWNLOAD_STATE=0xEEBB04;
	public static final int TAG_STATE_DOWNLOAD_OK=0xEEBB13;
	public static final int TAG_STATE_DOWNLOAD_ERROR=0xEEBB14;
}
