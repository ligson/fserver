package com.boful.net.client.event;

import java.io.File;

public interface TransferEvent {
    public void onStart(File src, File dest);

    public void onSuccess(File src, File dest);

    public void onTransfer(File src, File dest, int process);

    public void onFail(File src, File dest,String message);

}
