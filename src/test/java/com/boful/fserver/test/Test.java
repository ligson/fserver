package com.boful.fserver.test;

import java.io.File;

import com.boful.common.file.utils.FileUtils;

public class Test {
	public static void main(String[] args) {
		File file = new File("d:/ue_chinese.exe");
		byte[] hash = FileUtils.getHash(file);
		System.out.println(hash.length);
	}
}
