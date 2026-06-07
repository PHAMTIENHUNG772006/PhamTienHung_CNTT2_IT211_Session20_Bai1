package com.re.session20.security.jwt;

import com.re.session20.model.entity.Employee;
import com.re.session20.model.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtProvider {

    @Value("${jwt-secret}")
    private String jwtSecret;

    @Value("${jwt-expired}")
    private Long jwtExpired;

    @Value("${jwt-refresh-expired}")
    private Long jwtRefreshExpired;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateAccessToken(Employee employee){

        return Jwts.builder()
                .setSubject(employee.getUsername())
                .claim(
                        "roles",
                        employee.getRoles()
                                .stream()
                                .map(Role::getRoleName)
                                .toList()
                )
                .claim("type","ACCESS")
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + jwtExpired
                        )
                )
                .signWith(getSecretKey())
                .compact();
    }

    public String generateRefreshToken(Employee employee){

        return Jwts.builder()
                .setSubject(employee.getUsername())
                .claim("type","REFRESH")
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + jwtRefreshExpired
                        )
                )
                .signWith(getSecretKey())
                .compact();
    }

    public boolean validateToken(String token){

        try {

            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (UnsupportedJwtException
                 | ExpiredJwtException
                 | MalformedJwtException
                 | SignatureException
                 | IllegalArgumentException e) {

            return false;
        }
    }

    public String getUsernameFromToken(String token){

        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}