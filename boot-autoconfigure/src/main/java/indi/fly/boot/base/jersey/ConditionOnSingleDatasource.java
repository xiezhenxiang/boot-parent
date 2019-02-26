package indi.fly.boot.base.jersey;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Conditional({OnSingleDatasourceCondition.class})
public @interface ConditionOnSingleDatasource {
}
