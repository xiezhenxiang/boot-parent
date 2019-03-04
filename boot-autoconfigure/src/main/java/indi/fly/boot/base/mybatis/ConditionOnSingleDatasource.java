package indi.fly.boot.base.mybatis;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Conditional({MybatisScanAutoConfig.class})
public @interface ConditionOnSingleDatasource {
}
