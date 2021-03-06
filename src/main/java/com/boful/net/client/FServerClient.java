package com.boful.net.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.boful.common.file.utils.FileUtils;
import com.boful.convert.core.TranscodeEvent;
import com.boful.net.client.event.TransferEvent;
import com.boful.net.fserver.codec.BofulCodec;
import com.boful.net.fserver.protocol.DownloadProtocol;
import com.boful.net.fserver.protocol.TransferProtocol;

public class FServerClient {
    private BofulCodec bofulCodec = new BofulCodec();
    private NioSocketConnector connector = new NioSocketConnector();

    /***
     * 客户端业务处理
     * 
     * @see FServerClientHandler
     */
    private FServerClientHandler clientHandler = new FServerClientHandler();

    /***
     * 当前链接
     */
    private ConnectFuture cf;
    private IoSession ioSession;

    private Logger logger = Logger.getLogger(FServerClient.class);

    /***
     * 链接到服务器
     * 
     * @param address
     *            服务器地址
     * @param port
     *            服务器端口
     */
    public void connect(String address, int port) throws Exception {
        logger.debug("开始连接服务器：" + address + ":" + port);

        // 创建接受数据的过滤器
        DefaultIoFilterChainBuilder chain = connector.getFilterChain();

        // 设定这个过滤器将一行一行(/r/n)的读取数据
        chain.addLast("codec", new ProtocolCodecFilter(bofulCodec));

        // 客户端的消息处理器：一个SamplMinaServerHander对象
        connector.setHandler(clientHandler);

        // set connect timeout
        connector.setConnectTimeoutMillis(60 * 60 * 1000);

        // 连接到服务器：
        cf = connector.connect(new InetSocketAddress(address, port));
        cf.awaitUninterruptibly();
        try {
            ioSession = cf.getSession();
            logger.debug("服务器" + address + ":" + port + "连接成功！");
        } catch (Exception e) {
            logger.debug("服务器" + address + ":" + port + "连接失败！");
            throw e;
        }
    }

    /***
     * 断开链接
     */
    public void disconnect() {
        connector.dispose();
    }

    /***
     * 发送客户端文件到服务器的destFile
     * 
     * @param file
     *            要发送的文件
     * @param destFile
     *            目标文件
     * @throws Exception
     */
    public void send(File file, String destFile, TransferEvent transferEvent) throws Exception {
        if (ioSession != null) {
            clientHandler.setTransferEvent(transferEvent);
            InputStream inputStream = new FileInputStream(file);
            int bufferSize = 64 * 1024;
            byte[] buffer = new byte[bufferSize];
            int len = -1;
            long offset = 0;
            String fileHash = FileUtils.getHexHash(file);
            transferEvent.onStart(file, destFile);
            while ((len = inputStream.read(buffer)) > 0) {
                TransferProtocol transferProtocol = new TransferProtocol();
                transferProtocol.setSrcFile(file);
                transferProtocol.setDestFile(destFile);
                transferProtocol.setFileSize(file.length());
                transferProtocol.setLen(len);
                transferProtocol.setHash(fileHash);
                transferProtocol.setOffset(offset);
                transferProtocol.setBuffer(buffer);
                ioSession.write(transferProtocol);
                offset += bufferSize;

                int process = (int) (offset * 1.00 / file.length()) * 100;
                transferEvent.onTransfer(file, destFile, process);
            }

            inputStream.close();
        } else {
            throw new Exception("服务器连接失败！");
        }
    }

    public void download(File serverFile, String nativeFile, TransferEvent transferEvent) throws Exception {
        IoSession ioSession = cf.getSession();
        if (ioSession != null) {
            clientHandler.setTransferEvent(transferEvent);
            DownloadProtocol downloadProtocol = new DownloadProtocol();
            downloadProtocol.setSrc(serverFile);
            downloadProtocol.setDest(nativeFile);
            ioSession.write(downloadProtocol);
            transferEvent.onStart(serverFile, nativeFile);
        }
    }

    public void setCmd(String cmd) {
        ioSession.setAttribute("cmd", cmd);
    }

    public void setTranscodeEvent(TranscodeEvent event) {
        clientHandler = (FServerClientHandler) connector.getHandler();
    }

    private int serverIndex;

    public void setIndex(int index) {
        this.serverIndex = index;
    }

    public int getIndex() {
        return this.serverIndex;
    }
}
