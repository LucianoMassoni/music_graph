package com.luciano.music_graph.config;

import com.luciano.music_graph.model.RefreshToken;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.service.RefreshTokenService;
import com.luciano.music_graph.service.UserService;
import com.luciano.music_graph.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final CookieUtils cookieUtils;

    @Value("${front.url}")
    private String FRONT_URL;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken oAuthToken = (OAuth2AuthenticationToken) authentication;

        assert oAuth2User != null;
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String registrationId = oAuthToken.getAuthorizedClientRegistrationId();

        // busca o crea el usuario
        User user = userService.findByIdOrCreate(email, name, registrationId);

        // genera el refresh token
        RefreshToken refreshToken = refreshTokenService.create(user.getId());

        ResponseCookie cookie = cookieUtils.createRefreshTokenCookie(refreshToken.getToken());

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.sendRedirect(FRONT_URL);
    }
}
