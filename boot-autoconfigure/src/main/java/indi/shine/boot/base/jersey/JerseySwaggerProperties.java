package indi.shine.boot.base.jersey;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiezhenxiang 2019/6/13
 **/
@ConfigurationProperties(
        prefix = "jersey.swagger"
)
@Data
public class JerseySwaggerProperties {

    private String basePackage;
    private Boolean init = true;
    private String version;
    private String title = "Restful API";
    private String host;
    private String ip;
    private Integer port;
    private String basePath = "";
    private String resourcePackage;
    private String singleResource;
    private String otherResourcePackage;
    private String cdn;
}
