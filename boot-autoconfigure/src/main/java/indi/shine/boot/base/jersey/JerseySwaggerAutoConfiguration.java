package indi.shine.boot.base.jersey;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.collect.Sets;
import indi.shine.boot.base.api.SwaggerView;
import indi.shine.boot.base.exception.handler.BaseExceptionHandler;
import indi.shine.boot.base.exception.handler.ExceptionHandler;
import indi.shine.boot.base.exception.handler.ValidationExceptionMapper;
import indi.shine.boot.base.exception.handler.WebApplicationExceptionHandler;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.boot.autoconfigure.jersey.ResourceConfigCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.ws.rs.Path;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnClass(
        name = {"org.glassfish.jersey.server.spring.SpringComponentProvider", "javax.servlet.ServletRegistration"}
)
@ConditionalOnMissingBean(
        type = {"org.glassfish.jersey.server.ResourceConfig"}
)
@EnableConfigurationProperties({JerseySwaggerProperties.class, JerseyClientProperties.class})
@AutoConfigureBefore({JerseyAutoConfiguration.class})
public class JerseySwaggerAutoConfiguration extends ResourceConfig {
    private final JerseySwaggerProperties jersey;
    public JerseySwaggerAutoConfiguration(JerseySwaggerProperties jersey) {
        this.jersey = jersey;
    }

    @Bean
    public ResourceConfigCustomizer resourceRegister() {
        return (config) -> {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(Path.class));
            scanner.addIncludeFilter(new AnnotationTypeFilter(Provider.class));
            String otherResourcePackage = this.jersey.getOtherResourcePackage();
            Set<String> packages = Sets.newHashSet(this.jersey.getBasePackage());
            if (StringUtils.hasLength(otherResourcePackage)) {
                String[] var5 = StringUtils.tokenizeToStringArray(otherResourcePackage, ",");
                int var6 = var5.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    String className = var5[var7];
                    packages.add(className);
                }
            }

            Set<Class<?>> allClasses = Sets.newHashSet(JacksonJsonProvider.class, ValidationExceptionMapper.class, ExceptionHandler.class, BaseExceptionHandler.class, WebApplicationExceptionHandler.class);
            Iterator var10 = packages.iterator();

            while (var10.hasNext()) {
                String pkg = (String)var10.next();
                Set<Class<?>> collect = scanner.findCandidateComponents(pkg).stream().map((beanDefinition) ->
                     ClassUtils.resolveClassName(beanDefinition.getBeanClassName(), this.getClassLoader())
                ).collect(Collectors.toSet());
                allClasses.addAll(collect);
            }

            String classes = this.jersey.getSingleResource();
            if (StringUtils.hasLength(classes)) {
                allClasses.add(this.getExceptionHandlerClass(classes));
            }

            config.registerClasses(allClasses).property("jersey.config.beanValidation.enableOutputValidationErrorEntity.server", true).property("jersey.config.beanValidation.disable.validateOnExecutableCheck.server", true);
        };
    }

    private Class<?> getExceptionHandlerClass(String className) throws LinkageError {
        try {
            Class<?> exceptionClass = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
            Assert.isAssignable(ExceptionMapper.class, exceptionClass);
            return exceptionClass;
        } catch (ClassNotFoundException var3) {
            throw new ApplicationContextException("Failed to load exception handler class [" + className + "]", var3);
        }
    }

    @Bean
    @ConditionalOnClass(
            name = {"io.swagger.jaxrs.listing.ApiListingResource"}
    )
    @ConditionalOnProperty(
            prefix = "jersey.swagger",
            name = {"init"},
            havingValue = "true",
            matchIfMissing = true
    )
    public ResourceConfigCustomizer initSwagger2() {

        return (config) -> {
            config.registerClasses(ApiListingResource.class, SwaggerSerializers.class);
            BeanConfig beanConfig = new BeanConfig();
            beanConfig.setVersion(this.jersey.getVersion());
            beanConfig.setTitle(this.jersey.getTitle());
            if (Objects.nonNull(this.jersey.getHost())) {
                beanConfig.setHost(this.jersey.getHost());
            } else {
                beanConfig.setHost(this.jersey.getIp() + ":" + this.jersey.getPort());
            }

            beanConfig.setBasePath(this.jersey.getBasePath());
            beanConfig.setResourcePackage(this.jersey.getResourcePackage());
            beanConfig.setScan();
        };
    }

    @Bean
    @ConditionalOnClass(
            name = {"org.glassfish.jersey.media.multipart.MultiPartFeature"}
    )
    public ResourceConfigCustomizer registerMultiPartFeature() {
        return config -> config.registerClasses(MultiPartFeature.class);
    }

    @Bean
    @ConditionalOnClass(
            name = {"org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature"}
    )
    @ConditionalOnBean(
            name = {"initSwagger2"}
    )
    public ResourceConfigCustomizer initSwagger2UI() {
        return config ->
            config.property("jersey.config.server.mvc.templateBasePath.freemarker", "META-INF/resources").property("jersey.config.server.mvc.caching.freemarker", false).registerClasses(new Class[]{FreemarkerMvcFeature.class, SwaggerView.class});
    }


    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnClass(
            name = {"org.springframework.web.cors.CorsConfigurationSource"}
    )
    @ConditionalOnProperty(
            prefix = "filter",
            name = {"cross"},
            havingValue = "true",
            matchIfMissing = true
    )
    public CorsFilter corsFilter() {

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }

    @Bean
    @ConditionalOnClass(
            name = {"org.glassfish.jersey.client.JerseyClient", "org.glassfish.jersey.media.multipart.MultiPartFeature"}
    )
    @ConditionalOnMissingBean(
            name = {"jerseyHttp"}
    )
    public JerseyHttp jerseyHttp(JerseyClientProperties clientProperties) {
        return new JerseyHttp(clientProperties);
    }
}