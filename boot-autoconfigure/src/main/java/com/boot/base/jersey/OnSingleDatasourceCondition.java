package com.boot.base.jersey;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;

import javax.sql.DataSource;

@Configuration
@ConditionalOnClass({DataSource.class, SqlSessionFactory.class, MapperScan.class})
@ConditionalOnMissingBean({MapperFactoryBean.class})
public class OnSingleDatasourceCondition implements Condition {
    public OnSingleDatasourceCondition() {
    }

    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        String[] dbs = (String[])environment.getProperty("multiply.datasource.name", String[].class);
        if (dbs == null || dbs.length == 0) {
            ClassPathMapperScanner scanner = new ClassPathMapperScanner(context.getRegistry());
            ResourceLoader resourceLoader = context.getResourceLoader();
            if (resourceLoader != null) {
                scanner.setResourceLoader(resourceLoader);
            }

            scanner.registerFilters();
            String daoPackage = environment.getProperty("jersey.swagger.base-package") + ".dao";
            scanner.doScan(new String[]{daoPackage});
        }

        return false;
    }
}
