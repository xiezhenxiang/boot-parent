package indi.fly.boot.base.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("jwt")
public class JwtProperties {
    private String secretKey = "BOOT_SECRET";
    private String issuer = "boot";
    private Long refreshInterval = 3L;
    private Integer expireDate = Integer.valueOf(7);
    private Security security = new Security();

    public JwtProperties() {
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getIssuer() {
        return this.issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Long getRefreshInterval() {
        return this.refreshInterval;
    }

    public void setRefreshInterval(Long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public Integer getExpireDate() {
        return this.expireDate;
    }

    public void setExpireDate(Integer expireDate) {
        this.expireDate = expireDate;
    }

    public Security getSecurity() {
        return this.security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    class Security {

        private Boolean login = false;
        private List<String> ignoreUrls;

        public Boolean getLogin() {
            return this.login;
        }

        public void setLogin(Boolean login) {
            this.login = login;
        }

        public List<String> getIgnoreUrls() {
            return this.ignoreUrls;
        }

        public void setIgnoreUrls(List<String> ignoreUrls) {
            this.ignoreUrls = ignoreUrls;
        }
    }
}