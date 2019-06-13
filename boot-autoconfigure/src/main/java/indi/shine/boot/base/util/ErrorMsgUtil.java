package indi.shine.boot.base.util;

import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.Iterator;

public class ErrorMsgUtil {
    private static MutablePropertySources mutablePropertySources = new MutablePropertySources();

    public ErrorMsgUtil() {
    }

    public static String getErrMsg(Integer code) {
        Iterator var1 = mutablePropertySources.iterator();

        Object value;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            PropertySource<?> propertySource = (PropertySource)var1.next();
            value = propertySource.getProperty(code.toString());
        } while(value == null);

        return value.toString();
    }

    static {
        ResourcePropertySource resourcePropertySource;
        try {
            resourcePropertySource = new ResourcePropertySource("base-error-msg.properties");
            mutablePropertySources.addLast(resourcePropertySource);
        } catch (IOException var3) {
            // TODO
        }

        try {
            resourcePropertySource = new ResourcePropertySource("error-msg.properties");
            mutablePropertySources.addLast(resourcePropertySource);
        } catch (IOException var2) {
            // TODO
        }

    }
}
