package com.boful.net.fserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

import com.boful.common.file.utils.FileUtils;
import com.boful.net.fserver.protocol.DownloadProtocol;
import com.boful.net.fserver.protocol.Operation;
import com.boful.net.fserver.protocol.SendStateProtocol;
import com.boful.net.fserver.protocol.TransferProtocol;
import com.boful.net.fserver.utils.ConfigUtils;

public class HandlerUtil {

    private static Logger logger = Logger.getLogger(ServerHandler.class);

    public static void doDownLoad(IoSession session, DownloadProtocol downloadProtocol) {
        File dest = new File(downloadProtocol.getDest());
        File src = downloadProtocol.getSrc();
        try {
            if (src.exists()) {
                InputStream inputStream = new FileInputStream(src);
                int bufferSize = 64 * 1024;
                byte[] buffer = new byte[bufferSize];
                int len = -1;
                long offset = 0;
                String fileHash = FileUtils.getHexHash(src);
                while ((len = inputStream.read(buffer)) > 0) {
                    TransferProtocol transferProtocol = new TransferProtocol();
                    transferProtocol.setSrcFile(src);
                    transferProtocol.setDestFile(dest.getAbsolutePath());
                    transferProtocol.setFileSize(src.length());
                    transferProtocol.setLen(len);
                    transferProtocol.setHash(fileHash);
                    transferProtocol.setOffset(offset);
                    transferProtocol.setBuffer(buffer);
                    session.write(transferProtocol);
                    offset += bufferSize;
                }
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("TAG_STATE_DOWNLOAD", Operation.TAG_STATE_DOWNLOAD_ERROR);
        }
    }

    public static void doReceive(IoSession session, TransferProtocol transferProtocol) {
        File dest = ConfigUtils.getUploadPath(transferProtocol.getHash(), transferProtocol.getSrcFile().getName());
        double process = transferProtocol.getOffset() * 1.00 / transferProtocol.getFileSize();
        logger.debug("src:" + transferProtocol.getSrcFile().getAbsolutePath() + "-dest:"
                + transferProtocol.getDestFile() + "-接收进度:" + process * 100 + "%");
        try {
            if (!dest.exists()) {
                dest.getParentFile().mkdirs();
                dest.createNewFile();
            }
            String writerKey = dest.getAbsolutePath() + "_writer";
            Object object = session.getAttribute(writerKey);
            RandomAccessFile randomAccessFile = null;
            if (object != null) {
                randomAccessFile = (RandomAccessFile) object;
            } else {
                randomAccessFile = new RandomAccessFile(dest, "rw");
                session.setAttribute(writerKey, randomAccessFile);
            }

            randomAccessFile.seek(transferProtocol.getOffset());
            randomAccessFile.write(transferProtocol.getBuffer(), 0, transferProtocol.getLen());

            // offset+len > fileSize时，说明文件已经传输完
            if ((transferProtocol.getOffset() + transferProtocol.getLen()) >= transferProtocol.getFileSize()) {
                String fileHash = FileUtils.getHexHash(dest);
                String srcHash = transferProtocol.getHash();
                logger.info("src:" + transferProtocol.getSrcFile().getAbsolutePath() + "-dest:"
                        + dest.getAbsolutePath() + " 传输完成......................hash 是否一致：" + fileHash.equals(srcHash));
                randomAccessFile.close();
                session.removeAttribute(writerKey);
                // 向客户端发送信息
                SendStateProtocol sendStateProtocol = new SendStateProtocol();
                sendStateProtocol.setHash(srcHash);
                sendStateProtocol.setSrcFile(transferProtocol.getSrcFile());
                sendStateProtocol.setDestFile(dest.getAbsolutePath());
                // 文件hash不一致
                if (!fileHash.equals(srcHash)) {
                    sendStateProtocol.setState(Operation.TAG_STATE_SEND_ERROR);
                }
                session.write(sendStateProtocol);
            }
        } catch (IOException e) {
            logger.info("上传失败！");
            e.printStackTrace();
            // 向客户端发送信息
            SendStateProtocol sendStateProtocol = new SendStateProtocol();
            sendStateProtocol.setSrcFile(transferProtocol.getSrcFile());
            sendStateProtocol.setDestFile(transferProtocol.getDestFile());
            sendStateProtocol.setState(Operation.TAG_STATE_SEND_ERROR);
            session.write(sendStateProtocol);
        }
    }

}
