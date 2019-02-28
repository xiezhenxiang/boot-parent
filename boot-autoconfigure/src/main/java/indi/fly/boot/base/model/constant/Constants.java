package indi.fly.boot.base.model.constant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

class Constants {
    private Constants() {
    }

    private static final Log logger = LogFactory.getLog(Constants.class);
    private static ResourcePropertySource resourcePropertySource;

    static {
        String[] fileDirs = new String[]{"demo.properties", "file:/work/conf/demo.properties"};

        for (int i = 0; i < fileDirs.length; i ++){
            try {
                resourcePropertySource = new ResourcePropertySource(fileDirs[i]);
            } catch (IOException e) {
                continue;
            }
        }
        if(resourcePropertySource == null)
            logger.info("未加载到配置文件");
    }

    public static String getValue(String key) {
        Object value = resourcePropertySource.getProperty(key);
        return value == null ? null : value.toString();
    }
    //public final static String ip = getValue("ip");
}