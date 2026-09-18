package com.luciano.music_graph.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    @Value("${cookie.secure}")
    private boolean secure;

    @Value("${jwt.refresh.expiration}")
    private long refreshDurationMs;

    @Value("${jwt.access.expiration}")
    private long accessDurationMs;

    public ResponseCookie createRefreshTokenCookie(String token){
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(secure)
                .path("/api/auth")
                .maxAge(refreshDurationMs / 1000)
                .sameSite("Strict")
                .build();
    }

    public ResponseCookie deleteRefreshTokenCookie(){
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(secure)
                .path("/api/auth")
                .maxAge(0)
                .build();
    }

    public ResponseCookie createAccessTokenCookie(String token){
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(secure)
                .path("/api")
                .maxAge(accessDurationMs / 1000)
                .build();
    }

    public ResponseCookie deleteAccessTokenCookie(){
        return ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(secure)
                .path("/api")
                .maxAge(0)
                .build();
    }
}
