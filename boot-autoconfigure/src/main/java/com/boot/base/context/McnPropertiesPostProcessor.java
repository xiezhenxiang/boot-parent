package com.boot.base.context;

import com.google.common.collect.Maps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class McnPropertiesPostProcessor implements EnvironmentPostProcessor, Ordered {
    public static final String APP_BASE_PACKAGE_PROPERTY = "jersey.swagger.base-package";

    public McnPropertiesPostProcessor() {
    }

    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        Map<String, Object> mapProp = Maps.newHashMap();
        mapProp.put("jersey.swagger.base-package", ClassUtils.getPackageName(application.getMainApplicationClass()));
        mapProp.put("mcn.version", this.getClass().getPackage().getImplementationVersion());
        mapProp.put("logging.level." + mapProp.get("jersey.swagger.base-package") + ".dao", "info");
        propertySources.addLast(new MapPropertySource("mcn-map", mapProp));

        try {
            propertySources.addLast(new ResourcePropertySource("mcn-global-unique", "classpath:config/mcn.properties"));
        } catch (IOException var9) {
            ;
        }

        try {
            String[] activeProfiles = environment.getActiveProfiles();
            StringBuilder globalConfigName = (new StringBuilder("classpath:")).append("mcn-global");
            if (Objects.nonNull(activeProfiles) && activeProfiles.length > 0) {
                globalConfigName.append("-").append(activeProfiles[0]);
            }

            globalConfigName.append(".properties");
            propertySources.addLast(new ResourcePropertySource("mcn-global", globalConfigName.toString()));
        } catch (IOException var8) {
            ;
        }

        try {
            String path = this.getClass().getResource("").getPath();
            path = path.replaceFirst("file:", "jar:file:");
            path = "file:" + path.replace(ClassUtils.getPackageName(this.getClass()).replace(".", "/"), "META-INF");
            propertySources.addLast(new PropertiesPropertySource("boot-default", PropertiesLoaderUtils.loadProperties(new UrlResource(path + "boot-default.properties"))));
        } catch (IOException var7) {
            //var7.printStackTrace();
        }

        try {
            String path = this.getClass().getResource("").getPath();
            path = path.replaceFirst("file:", "jar:file:");
            path = path.replace(ClassUtils.getPackageName(this.getClass()).replace(".", "/"), "META-INF");
            propertySources.addLast(new PropertiesPropertySource("boot-default", PropertiesLoaderUtils.loadProperties(new UrlResource(path + "boot-default.properties"))));
        } catch (IOException var7) {
            //var7.printStackTrace();
        }

    }

    public int getOrder() {
        return -2147483637;
    }
}
