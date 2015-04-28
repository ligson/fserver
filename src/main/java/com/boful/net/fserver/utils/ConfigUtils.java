package com.boful.net.fserver.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ConfigUtils {
    private static Logger logger = Logger.getLogger(ConfigUtils.class);

    public static int[] initServerConfig() {
        int[] config = new int[3];
        try {
            URL url = ClassLoader.getSystemResource("conf/config.properties");
            if (url == null) {
                url = ClassLoader.getSystemResource("config.properties");
            }
            InputStream in = new BufferedInputStream(new FileInputStream(url.getPath()));
            Properties props = new Properties();
            props.load(in);

            // 取得内容
            int bufferSize = Integer.parseInt(props.getProperty("server.bufferSize"));
            int idleTime = Integer.parseInt(props.getProperty("server.idleTime"));
            int port = Integer.parseInt(props.getProperty("server.port"));

            config[0] = bufferSize;
            config[1] = idleTime;
            config[2] = port;

            return config;
        } catch (Exception e) {
            logger.debug("配置文件初始化失败...........");
            logger.debug("错误信息：" + e.getMessage());
            return config;
        }

    }
}
