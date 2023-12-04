package com.kingdee.action;

import java.io.*;
import java.util.Properties;

public class PropertieUtils {
    public static final String webPath = "webpath";
    public static final String savePath = "savepath";
    public static final String version = "version";
    public static final String describe = "describe";
    public static final Properties properties = new Properties();
    public static final String filePath = "C:\\plugin.properties";

    public static Properties loadPropertiesValue() {
        try {
            InputStream inputStream = new FileInputStream(filePath);
            properties.load(inputStream);
        } catch (IOException e) {

        }
        return properties;
    }

    public static void writePropertiesValue(Properties properties) {
        try {
            OutputStream outputStream = new FileOutputStream(filePath);
            properties.store(outputStream, null);
        } catch (IOException ignore) {

        }
    }
}
