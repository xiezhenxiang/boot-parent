package indi.shine.boot.base.context;

import indi.shine.boot.base.util.BeanUtil;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;

/**
 * @author xiezhenxiang 2019/6/13
 **/
@Order(-2147483638)
public class McnContextInitializer implements ApplicationContextInitializer {
    public McnContextInitializer() {
    }

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        configurableApplicationContext.addApplicationListener((ContextRefreshedEvent event) ->
            new BeanUtil().setApplicationContext(event.getApplicationContext())
        );
    }
}

