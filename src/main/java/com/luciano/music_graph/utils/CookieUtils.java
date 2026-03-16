package com.luciano.music_graph.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    @Value("${cookie.secure}")
    private boolean secure;

    @Value("${jwt.refresh.expiration}")
    private long durationMs;

    public ResponseCookie createRefreshTokenCookie(String token){
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(secure)
                .path("api/auth")
                .maxAge(durationMs / 1000)
                .sameSite("Strict")
                .build();
    }

    public ResponseCookie deleteRefreshTokenCookie(){
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(secure)
                .path("api/auth")
                .maxAge(0)
                .build();
    }
}
