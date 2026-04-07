package com.luciano.music_graph.service;

import com.luciano.music_graph.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.access.secret_key}")
    private String SECRET_KEY;

    @Value("${jwt.access.expiration}")
    private Long expirationMs;

    public String generateToken(User user){
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .claim("role", user.getRole())
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Key getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserId(String token){
        return extractAllClaims(token).getSubject();
    }

    public Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token){
        return  extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token, User user){
        try {
            final String userId = extractUserId(token);
            return userId.equals(user.getId().toString()) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }
}
