package com.oceanduty.util;

import com.oceanduty.common.domain.RequestUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT 令牌工具
 */
@Component
public class JwtUtil {

    @Value("${ocean-duty.jwt.secret}")
    private String secret;

    @Value("${ocean-duty.jwt.expire-hours}")
    private int expireHours;

    /**
     * 生成 JWT 令牌
     */
    public String generateToken(Long userId, String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expireHours, ChronoUnit.HOURS)))
                .signWith(getSignKey())
                .compact();
    }

    /**
     * 解析 JWT 令牌
     */
    public RequestUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return RequestUser.builder()
                .userId(Long.parseLong(claims.getSubject()))
                .username(claims.get("username", String.class))
                .role(claims.get("role", String.class))
                .build();
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
