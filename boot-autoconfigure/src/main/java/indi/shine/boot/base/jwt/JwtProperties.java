package indi.shine.boot.base.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties("jwt")
@Component
@Data
public class JwtProperties {

    private String secretKey = "BOOT_SECRET";
    private String issuer = "boot";
    private Long refreshInterval = 3L;
    private Integer expireDate = 7;
    private Security security = new Security();

    @Data
    class Security {
        private Boolean login = false;
        private List<String> ignoreUrls;
    }
}