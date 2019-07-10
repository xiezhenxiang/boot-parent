package indi.shine.boot.base.model.constant;

import indi.shine.boot.base.util.ClassUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

class Constants {

    private static final Log logger = LogFactory.getLog(Constants.class);
    private static Properties props = new Properties();

    static {
        String appDir = ClassUtil.getProjectDir(Constants.class);
        String localPath = appDir + "classes/config/application.properties";
        String prodPath = appDir.replace("/lib", "/conf") + "application.properties";
        String[] filePath= new String[]{localPath, prodPath};

        for (String path : filePath){
            try {
                props.load(new FileInputStream(path));
            } catch (IOException e) {
                // todo
            }
        }
        if (props.isEmpty()) {
            logger.info("未加载到配置文件");
        }
    }
}