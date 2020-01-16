package indi.shine.boot.base.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.ResourcePropertySource;
import java.io.IOException;
import java.util.Iterator;

@Slf4j
public class CodeMsgUtil {

    private static final String BASE_MSG_FILE_NAME = "base-error-msg.properties";
    private static final String SERVER_MSG_FILE_NAME = "error-msg.properties";

    private static MutablePropertySources mutablePropertySources = new MutablePropertySources();
    public CodeMsgUtil() {
    }

    static {
        ResourcePropertySource resourcePropertySource;
        try {
            resourcePropertySource = new ResourcePropertySource(BASE_MSG_FILE_NAME);
            mutablePropertySources.addLast(resourcePropertySource);
        } catch (IOException e) {
            // nothing
        }
        try {
            resourcePropertySource = new ResourcePropertySource(SERVER_MSG_FILE_NAME);
            mutablePropertySources.addLast(resourcePropertySource);
        } catch (IOException e) {
            // nothing
        }
    }

    public static String getMsg(Integer code) {

        Iterator iterator = mutablePropertySources.iterator();
        String msg = "";
        while (iterator.hasNext()) {
            PropertySource<?> propertySource = (PropertySource)iterator.next();
            if (propertySource.containsProperty(code.toString()) && propertySource.getProperty(code.toString()) != null) {
                msg =  propertySource.getProperty(code.toString()).toString();
                break;
            }
        }
        if ("".equals(msg)) {
            log.warn("not find msg by code: {}", code);
        }
        return msg;
    }
}
