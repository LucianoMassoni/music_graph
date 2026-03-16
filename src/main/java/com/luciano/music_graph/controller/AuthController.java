package com.luciano.music_graph.controller;

import com.luciano.music_graph.dto.AuthResponse;
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
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request){

        AuthTokens tokens = authService.register(request);
        ResponseCookie cookie = cookieUtils.createRefreshTokenCookie(tokens.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(tokens.accessToken()));
    }

    @PostMapping("login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){

        AuthTokens tokens = authService.login(request);
        ResponseCookie cookie = cookieUtils.createRefreshTokenCookie(tokens.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(tokens.accessToken()));
    }

    @PostMapping("refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = "refreshToken", required = false) String refreshTokenString){

        if (refreshTokenString == null) throw new RefreshTokenNotFoundException("cookie missing");

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenString);
        AuthTokens tokens = authService.refresh(refreshToken);
        ResponseCookie newCookie = cookieUtils.createRefreshTokenCookie(tokens.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newCookie.toString())
                .body(new AuthResponse(tokens.accessToken()));
    }

    @PostMapping("logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken", required = false) String refreshTokenString){

        if (refreshTokenString == null) throw new RefreshTokenNotFoundException("cookie missing");

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenString);
        authService.logout(refreshToken);
        ResponseCookie cookie = cookieUtils.deleteRefreshTokenCookie();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
