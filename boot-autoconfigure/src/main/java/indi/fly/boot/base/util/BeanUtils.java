package indi.fly.boot.base.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class BeanUtils implements ApplicationContextAware {
    private static ApplicationContext ac;

    public BeanUtils() {
    }

    public static Object getBean(String name) {
        checkApplicationContext();
        return ac.getBean(name);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        checkApplicationContext();
        return ac.getBean(name, clazz);
    }

    public static <T> T getBean(Class<T> clazz) {
        checkApplicationContext();
        return ac.getBean(clazz);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ac = applicationContext;
    }

    private static void checkApplicationContext() {
        if (ac == null) {
            throw new IllegalStateException("applicationContext not inject yet");
        }
    }
}