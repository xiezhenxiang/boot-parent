package indi.shine.boot.base.jersey;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "jersey.swagger"
)
public class JerseySwaggerProperties {
    private String basePackage;
    private Boolean init = true;
    private String version;
    private String title = "API";
    private String host;
    private String ip;
    private Integer port;
    private String basePath = "";
    private String resourcePackage;
    private String singleResource;
    private String otherResourcePackage;
    private String cdn;

    public JerseySwaggerProperties() {
    }

    public String getBasePackage() {
        return this.basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public Boolean getInit() {
        return this.init;
    }

    public void setInit(Boolean init) {
        this.init = init;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getBasePath() {
        return this.basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getResourcePackage() {
        return this.resourcePackage;
    }

    public void setResourcePackage(String resourcePackage) {
        this.resourcePackage = resourcePackage;
    }

    public String getSingleResource() {
        return this.singleResource;
    }

    public void setSingleResource(String singleResource) {
        this.singleResource = singleResource;
    }

    public String getOtherResourcePackage() {
        return this.otherResourcePackage;
    }

    public void setOtherResourcePackage(String otherResourcePackage) {
        this.otherResourcePackage = otherResourcePackage;
    }

    public String getCdn() {
        return this.cdn;
    }

    public void setCdn(String cdn) {
        this.cdn = cdn;
    }
}
