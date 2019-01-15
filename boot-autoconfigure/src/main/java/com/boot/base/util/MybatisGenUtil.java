package com.boot.base.util;


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

        genMapperAndXml(Boolean.valueOf(prop.getProperty("overwrite", "false")).booleanValue());
    }

    private static void genMapperAndXml(boolean overwrite) {
        List<String> warnings = new ArrayList();
        ConfigurationParser cfgParser = new ConfigurationParser(warnings);
        Configuration config = null;

        try {
            config = cfgParser.parseConfiguration(MybatisGenUtil.class.getClassLoader().getResourceAsStream("generatorConfig.xml"));
        } catch (IOException var11) {
            var11.printStackTrace();
        } catch (XMLParserException var12) {
            var12.printStackTrace();
        }

        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        MyBatisGenerator generator = null;

        try {
            generator = new MyBatisGenerator(config, callback, warnings);
        } catch (InvalidConfigurationException var10) {
            var10.printStackTrace();
        }

        try {
            generator.generate((ProgressCallback)null);
            System.out.println("mybatis generator success");
        } catch (SQLException var7) {
            var7.printStackTrace();
        } catch (IOException var8) {
            var8.printStackTrace();
        } catch (InterruptedException var9) {
            var9.printStackTrace();
        }

    }
}