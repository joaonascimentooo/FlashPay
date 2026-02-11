package com.flashpay.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    
    @Value("${jwt.secret:mySecretKeyThatShouldBeAtLeast256BitsLongForHS256Algorithm}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;
    
    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenExpirationMs;
    
    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(userDetails.getUsername())
                
                .claim("authorities", 
                    userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
                )
                
                .issuedAt(now)
                .expiration(expiryDate)
                
                .signWith(key)
                
                .compact();
    }

    public String getEmailFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.warn("Erro ao extrair email do token: {}", e.getMessage());
            return null;
        }
    }

    private Claims getAllClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
   
    private Boolean isTokenExpired(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            Date expirationDate = claims.getExpiration();
            return expirationDate.before(new Date());
        } catch (Exception e) {
            log.warn("Erro ao verificar expiração do token: {}", e.getMessage());
            return true; 
        }
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String email = getEmailFromToken(token);
            return (email != null 
                    && email.equals(userDetails.getUsername()) 
                    && !isTokenExpired(token));
        } catch (Exception e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    public Boolean isTokenValid(String token) {
        try {
            getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    public long getExpirationTimeInSeconds() {
        return jwtExpirationMs / 1000;
    }
   
    public String generateRefreshToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(userDetails.getUsername())
                
                .claim("type", "refresh")
                
                .issuedAt(now)
                .expiration(expiryDate)
                
                .signWith(key)
                
                .compact();
    }

    public String getTokenType(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            Object typeObj = claims.get("type");
            return typeObj != null ? typeObj.toString() : "access";
        } catch (Exception e) {
            return null;
        }
    }

    public Boolean isRefreshTokenValid(String token) {
        try {
            String type = getTokenType(token);
            return "refresh".equals(type) && isTokenValid(token);
        } catch (Exception e) {
            log.warn("Refresh token inválido: {}", e.getMessage());
            return false;
        }
    }

    public long getRefreshTokenExpirationTimeInSeconds() {
        return refreshTokenExpirationMs / 1000;
    }

    public static Date convertLocalDateTimeToDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
