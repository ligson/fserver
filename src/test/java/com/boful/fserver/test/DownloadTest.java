package com.boful.fserver.test;

import java.io.File;

import com.boful.net.client.FServerClient;

public class DownloadTest {

    public static void main(String[] args) throws Exception {
        FServerClient fServerClient = new FServerClient();
        fServerClient.connect("192.168.1.75", 8000);
        
        fServerClient.download(new File("E:/test/convert/7867C06EA8975704CA1B1D5DB87FC3CB.f4v"), "E:/test/bak.mp4", new TestTransferEvent());
    }

}
