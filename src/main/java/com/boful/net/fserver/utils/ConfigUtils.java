package com.boful.net.fserver.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.boful.common.file.utils.FileUtils;

public class ConfigUtils {
    private static Logger logger = Logger.getLogger(ConfigUtils.class);

    private static File uploadPath = new File("");

    public static int[] initServerConfig() {
        logger.debug("配置文件初始化。。。。。。");
        int[] config = new int[4];
        try {
            URL url = ClassLoader.getSystemResource("conf/config.properties");
            if (url == null) {
                url = ClassLoader.getSystemResource("config.properties");
            }
            //InputStream in = new BufferedInputStream(new FileInputStream(url.getPath()));
            InputStream in = new BufferedInputStream(new FileInputStream(new File("src/main/resources/config.properties")));
            Properties props = new Properties();
            props.load(in);

            // 取得内容
            int bufferSize = Integer.parseInt(props.getProperty("server.bufferSize"));
            int idleTime = Integer.parseInt(props.getProperty("server.idleTime"));
            int port = Integer.parseInt(props.getProperty("server.port"));

            uploadPath = new File(props.getProperty("upload.path"));
            if (!uploadPath.exists()) {
                uploadPath.mkdirs();
            }

            config[0] = bufferSize;
            config[1] = idleTime;
            config[2] = port;
            logger.debug("配置文件初始化成功！");
            return config;
        } catch (Exception e) {
            logger.debug("配置文件初始化失败！");
            logger.debug("错误信息：" + e.getMessage());
            return config;
        }
    }

    /**
     * 根据上传文件的Hash和类型得到新的文件路径
     * 
     * @param fileHash
     *            文件Hash
     * @param fileName
     *            文件名
     * @return 文件路径
     */
    public static File getUploadPath(String fileHash, String fileName) {
        String fileSufix = FileUtils.getFileSufix(fileName);
        return new File(uploadPath, fileHash + "." + fileSufix);
    }
}
