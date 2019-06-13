package indi.shine.boot.base.util;


import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MybatisGenUtil {
    public MybatisGenUtil() {
    }

    public static void genMapperAndXml() {
        Properties prop = new Properties();
        try {
            prop.load(MybatisGenUtil.class.getClassLoader().getResourceAsStream("generator.properties"));
        } catch (IOException var2) {
            var2.printStackTrace();
        }

        genMapperAndXml(Boolean.valueOf(prop.getProperty("overwrite", "false")));
    }

    private static void genMapperAndXml(boolean overwrite) {
        List<String> warnings = new ArrayList<>();
        ConfigurationParser cfgParser = new ConfigurationParser(warnings);
        Configuration config = null;

        try {
            config = cfgParser.parseConfiguration(MybatisGenUtil.class.getClassLoader().getResourceAsStream("generatorConfig.xml"));
        } catch (Exception var11) {
            var11.printStackTrace();
        }

        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        MyBatisGenerator generator = null;

        try {
            generator = new MyBatisGenerator(config, callback, warnings);
        } catch (InvalidConfigurationException var10) {
            var10.printStackTrace();
        }

        try {
            generator.generate(null);
            System.out.println("mybatis generator success");
        } catch (Exception var8) {
            var8.printStackTrace();
        }

    }
}