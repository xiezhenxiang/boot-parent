package indi.shine.boot.base.model.constant;

import indi.shine.boot.base.util.ClassUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

class Constants {
    private Constants() {
    }

    private static final Log logger = LogFactory.getLog(Constants.class);
    private static ResourcePropertySource props;

    static {
        String prodPath = ClassUtil.getProjectDir() + "/conf/demo.properties";
        String[] filePath= new String[]{"demo.properties", prodPath};

        for (String path : filePath){
            try {
                props = new ResourcePropertySource(path);
            } catch (IOException e) {
                // todo
            }
        }
        if (props == null)
            logger.info("未加载到配置文件");
    }

    public static String getValue(String key) {
        Object value = props.getProperty(key);
        return value == null ? null : value.toString();
    }
}