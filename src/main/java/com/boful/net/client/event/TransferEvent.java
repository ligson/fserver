package com.boful.net.client.event;

import java.io.File;

public interface TransferEvent {
    public void onStart(File src, String dest);

    public void onSuccess(File src, String dest);

    public void onTransfer(File src, String dest, int process);

    public void onFail(File src, String dest,String message);

}
