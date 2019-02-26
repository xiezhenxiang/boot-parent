package indi.fly.boot.base.context;

import indi.fly.boot.base.util.BeanUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;

@Order(-2147483638)
public class McnContextInitializer implements ApplicationContextInitializer {
    public McnContextInitializer() {
    }

    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        configurableApplicationContext.addApplicationListener((ContextRefreshedEvent event) -> {
            (new BeanUtils()).setApplicationContext(event.getApplicationContext());
        });
    }
}

