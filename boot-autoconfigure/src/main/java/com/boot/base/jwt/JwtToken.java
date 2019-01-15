package com.boot.base.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class JwtToken {
    private static JwtProperties jwtProperties;
    private HttpServletRequest request;
    private HttpServletResponse response;

    public JwtToken(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public JwtToken(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public JwtToken(){}

    public String createToken(Object identifier) {
        Map<String, Object> data = Maps.newHashMap();
        data.put("userId", identifier);
        return this.createToken((Map)data);
    }

    public String createToken(Map<String, Object> data) {
        if (!Objects.isNull(data) && !data.isEmpty()) {
            Date iaDate = new Date();
            Calendar nowTime = Calendar.getInstance();
            nowTime.add(5, this.jwtProperties.getExpireDate().intValue());
            Date expireDate = nowTime.getTime();
            Map<String, Object> map = Maps.newHashMap();
            map.put("alg", "HS256");
            map.put("typ", "JWT");
            Builder builder = JWT.create().withHeader(map).withExpiresAt(expireDate).withIssuedAt(iaDate).withIssuer(jwtProperties.getIssuer());
            this.convertDataToActualType(data, builder);
            String token = builder.sign(this.getAlgorithm());
            this.returnToken(token);
            return token;
        } else {
            return null;
        }
    }

    private void convertDataToActualType(Map<String, Object> data, Builder builder) {
        data.forEach((k, v) -> {
            if (v instanceof Integer) {
                builder.withClaim(k, ((Integer)v).intValue());
            } else if (v instanceof Double) {
                builder.withClaim(k, ((Double)v).doubleValue());
            } else if (v instanceof Date) {
                builder.withClaim(k, (Date)v);
            } else if (v instanceof Boolean) {
                builder.withClaim(k, ((Boolean)v).booleanValue());
            } else {
                builder.withClaim(k, v.toString());
            }

        });
    }

    private String checkIsCreateNewToken(DecodedJWT jwt) {
        Date issuedAt = jwt.getIssuedAt();
        if (System.currentTimeMillis() - issuedAt.getTime() > this.jwtProperties.getRefreshInterval().longValue() * 24L * 60L * 60L * 1000L) {
            Map<String, Claim> claims = jwt.getClaims();
            Map<String, Object> data = Maps.newHashMap();
            claims.forEach((k, v) -> {
                if (!"iat".equals(k) && !"exp".equals(k) && !"iss".equals(k)) {
                    data.put(k, v.as(Object.class));
                }

            });
            return this.createToken((Map)data);
        } else {
            return null;
        }
    }

    private Algorithm getAlgorithm() {
        try {
            return Algorithm.HMAC256(jwtProperties.getSecretKey());
        } catch (UnsupportedEncodingException var2) {
            throw new RuntimeException("The Secret Character Encoding is not supported");
        }
    }

    public DecodedJWT checkToken(String token) {
        JWTVerifier verifier = JWT.require(this.getAlgorithm()).build();
        DecodedJWT jwt = verifier.verify(token);
        this.checkIsCreateNewToken(jwt);
        return jwt;
    }

    public Integer getUserIdAsInt() {
        return this.getValueAsInt("userId");
    }

    public String getUserIdAsString() {
        return this.getValueByKey("userId");
    }

    public Integer getValueAsInt(String key) {
        return (Integer)this.getValueByKey(key, Integer.class);
    }

    public Long getValueAsLong(String key) {
        return (Long)this.getValueByKey(key, Long.class);
    }

    public Double getValueAsDouble(String key) {
        return (Double)this.getValueByKey(key, Double.class);
    }

    public Boolean getValueAsBoolean(String key) {
        return (Boolean)this.getValueByKey(key, Boolean.class);
    }

    public String getValueByKey(String key) {
        return (String)this.getValueByKey(key, String.class);
    }

    private <T> T getValueByKey(String key, Class<T> cls) {
        return this.getClaim(key).as(cls);
    }

    private Claim getClaim(String key) {
        return this.checkToken(this.getToken()).getClaim(key);
    }

    public String getToken() {
        HttpServletRequest request = this.getRequest();
        if (request == null) {
            return null;
        } else {
            String authorization = request.getHeader("Authorization");
            if (StringUtils.isBlank(authorization)) {
                return null;
            } else {
                return authorization;
            }
        }
    }

    private HttpServletRequest getRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(attributes)) {
            HttpServletRequest request = ((ServletRequestAttributes)attributes).getRequest();
            if (Objects.nonNull(request)) {
                this.request = request;
            }
        }

        return this.request;
    }

    private HttpServletResponse getResponse() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(attributes)) {
            HttpServletResponse response = ((ServletRequestAttributes)attributes).getResponse();
            if (Objects.nonNull(response)) {
                this.response = response;
            }
        }

        return this.response;
    }

    private void returnToken(String token) {
        HttpServletResponse response = this.getResponse();
        if (response != null) {
            response.setHeader("Authorization", token);
        }
    }
}
