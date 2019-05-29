package indi.shine.boot.base.context;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;

public class McnApplicationListener implements GenericApplicationListener {
    public static final int DEFAULT_ORDER = -2147483638;
    private int order = -2147483638;
    private static Class<?>[] EVENT_TYPES = new Class[]{ApplicationStartingEvent.class, ApplicationEnvironmentPreparedEvent.class, ApplicationPreparedEvent.class, ApplicationFailedEvent.class};
    private static Class<?>[] SOURCE_TYPES = new Class[]{SpringApplication.class};

    public McnApplicationListener() {
    }

    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ApplicationStartingEvent) {
            ;
        }

        if (applicationEvent instanceof ApplicationEnvironmentPreparedEvent) {
            ;
        }

        if (applicationEvent instanceof ApplicationPreparedEvent) {
            ;
        }

        if (applicationEvent instanceof ApplicationFailedEvent) {
            ;
        }

    }

    public boolean supportsEventType(ResolvableType resolvableType) {
        return this.isAssignableFrom(resolvableType.getRawClass(), EVENT_TYPES);
    }

    public boolean supportsSourceType(Class<?> sourceType) {
        return this.isAssignableFrom(sourceType, SOURCE_TYPES);
    }

    private boolean isAssignableFrom(Class<?> type, Class... supportedTypes) {
        if (type != null) {
            Class[] var3 = supportedTypes;
            int var4 = supportedTypes.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Class<?> supportedType = var3[var5];
                if (supportedType.isAssignableFrom(type)) {
                    return true;
                }
            }
        }

        return false;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
