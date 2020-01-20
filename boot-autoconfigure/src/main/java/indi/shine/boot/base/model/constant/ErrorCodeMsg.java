package indi.shine.boot.base.model.constant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

@Slf4j
public class ErrorCodeMsg {

    private static final String MSG_FILE_NAME = "error-msg.properties";

    private static Properties properties;
    static {
        try {
            properties = PropertiesLoaderUtils.loadAllProperties(MSG_FILE_NAME);
        } catch (IOException e) {
            log.error("load error-msg.properties file error!", e);
        }
    }

    public static String of(Integer code) {
        return properties.containsKey(code.toString())
            ? properties.getProperty(code.toString())
            : properties.getProperty(BaseErrorCode.UNKNOWN_ERROR + "");
    }
}
