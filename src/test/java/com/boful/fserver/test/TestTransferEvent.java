package com.boful.fserver.test;

import java.io.File;

import com.boful.net.client.event.TransferEvent;

public class TestTransferEvent implements TransferEvent {

    @Override
    public void onStart(File src, String dest) {
        System.out.println("onStart");
    }

    @Override
    public void onSuccess(File src, String dest) {
        System.out.println("onSuccess");
    }

    @Override
    public void onTransfer(File src, String dest, int process) {
        System.out.println("process:"+process);
    }

    @Override
    public void onFail(File src, String dest, String message) {
        System.out.println("onFail");
    }

}
