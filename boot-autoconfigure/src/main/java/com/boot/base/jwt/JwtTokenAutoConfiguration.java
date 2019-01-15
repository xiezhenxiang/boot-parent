package com.boot.base.jwt;

import com.auth0.jwt.JWT;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({JWT.class})
@EnableConfigurationProperties
public class JwtTokenAutoConfiguration {
    public JwtTokenAutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean(
            name = {"jwtToken"}
    )
    public JwtToken jwtToken() {
        return new JwtToken(this.jwtProperties());
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtProperties jwtProperties() {
        return new JwtProperties();
    }
}