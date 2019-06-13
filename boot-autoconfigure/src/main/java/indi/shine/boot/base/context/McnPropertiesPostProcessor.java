package indi.shine.boot.base.context;

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
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * @author xiezhenxiang 2019/6/13
 **/
public class McnPropertiesPostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final String JERSEY_BASE_PACKAGE = "jersey.swagger.base-package";

    public McnPropertiesPostProcessor() {
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        Map<String, Object> mapProp = Maps.newHashMap();
        mapProp.put(JERSEY_BASE_PACKAGE, ClassUtils.getPackageName(application.getMainApplicationClass()));
        mapProp.put("shine.version", this.getClass().getPackage().getImplementationVersion());

        mapProp.put("logging.level." + mapProp.get(JERSEY_BASE_PACKAGE) + ".dao", "info");
        propertySources.addLast(new MapPropertySource("init_prop", mapProp));


        try {
            String path = this.getClass().getResource("").getPath();
            path = path.replaceFirst("file:", "jar:file:");
            path = "file:" + path.replace(ClassUtils.getPackageName(this.getClass()).replace(".", "/"), "META-INF");
            propertySources.addLast(new PropertiesPropertySource("boot-default", PropertiesLoaderUtils.loadProperties(new UrlResource(path + "boot-default.properties"))));
        } catch (IOException e) {
            // TODO
        }

        try {
            String path = this.getClass().getResource("").getPath();
            path = path.replaceFirst("file:", "jar:file:");
            path = path.replace(ClassUtils.getPackageName(this.getClass()).replace(".", "/"), "META-INF");
            propertySources.addLast(new PropertiesPropertySource("boot-default", PropertiesLoaderUtils.loadProperties(new UrlResource(path + "boot-default.properties"))));
        } catch (IOException e) {
            // TODO
        }

    }

    @Override
    public int getOrder() {
        return -2147483637;
    }
}
