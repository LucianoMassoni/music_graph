package com.luciano.music_graph.controller;

import com.luciano.music_graph.dto.AuthTokens;
import com.luciano.music_graph.dto.LoginRequest;
import com.luciano.music_graph.dto.RegisterRequest;
import com.luciano.music_graph.exception.RefreshTokenNotFoundException;
import com.luciano.music_graph.model.RefreshToken;
import com.luciano.music_graph.service.AuthService;
import com.luciano.music_graph.service.RefreshTokenService;
import com.luciano.music_graph.utils.CookieUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth/")
public class AuthController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request){

        AuthTokens tokens = authService.register(request);
        ResponseCookie refreshCookie = cookieUtils.createRefreshTokenCookie(tokens.refreshToken());
        ResponseCookie accessCookie = cookieUtils.createAccessTokenCookie(tokens.accessToken());

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .build();
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){
        try {
            AuthTokens tokens = authService.login(request);
            ResponseCookie refreshCookie = cookieUtils.createRefreshTokenCookie(tokens.refreshToken());
            ResponseCookie accessCookie = cookieUtils.createAccessTokenCookie(tokens.accessToken());

            return ResponseEntity.noContent()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("refresh")
    public ResponseEntity<Void> refresh(@CookieValue(name = "refreshToken", required = false) String refreshTokenString){

        if (refreshTokenString == null) throw new RefreshTokenNotFoundException("cookie missing");

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenString);
        AuthTokens tokens = authService.refresh(refreshToken);

        ResponseCookie refreshCookie = cookieUtils.createRefreshTokenCookie(tokens.refreshToken());
        ResponseCookie accessCookie = cookieUtils.createAccessTokenCookie(tokens.accessToken());

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .build();
    }

    @PostMapping("logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken", required = false) String refreshTokenString){

        if (refreshTokenString == null) throw new RefreshTokenNotFoundException("cookie missing");

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenString);
        authService.logout(refreshToken);
        ResponseCookie refreshCookie = cookieUtils.deleteRefreshTokenCookie();
        ResponseCookie accessCookie = cookieUtils.deleteAccessTokenCookie();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .build();
    }
}
