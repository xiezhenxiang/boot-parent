package indi.fly.boot.base.jersey;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "jersey.client"
)
public class JerseyClientProperties {
    private String acceptContentType = "application/json;charset=utf-8";
    private String requestContentEncode = "application/x-www-form-urlencoded;charset=utf-8";
    private Integer connectTimeout = 120000;
    private Integer readTimeout = 120000;

    public JerseyClientProperties() {
    }

    public Integer getConnectTimeout() {
        return this.connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getReadTimeout() {
        return this.readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getAcceptContentType() {
        return this.acceptContentType;
    }

    public void setAcceptContentType(String acceptContentType) {
        this.acceptContentType = acceptContentType;
    }

    public String getRequestContentEncode() {
        return this.requestContentEncode;
    }

    public void setRequestContentEncode(String requestContentEncode) {
        this.requestContentEncode = requestContentEncode;
    }
}
