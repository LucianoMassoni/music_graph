package com.luciano.music_graph.config;

import com.luciano.music_graph.model.RefreshToken;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.service.JwtService;
import com.luciano.music_graph.service.RefreshTokenService;
import com.luciano.music_graph.service.UserService;
import com.luciano.music_graph.utils.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final CookieUtils cookieUtils;
    private final ObjectMapper mapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken oAuthToken = (OAuth2AuthenticationToken) authentication;

        assert oAuth2User != null;
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String registrationId = oAuthToken.getAuthorizedClientRegistrationId();

        // busca o crea el usuario
        User user = userService.findByIdOrCreate(email, name, registrationId);

        // genera el access token
        String accessToken = jwtService.generateToken(user);

        // genera el refresh token
        RefreshToken refreshToken = refreshTokenService.create(user.getId());

        ResponseCookie cookie = cookieUtils.createRefreshTokenCookie(refreshToken.getToken());

        // crea un mapper para no hacer json a mano
        String json = mapper.writeValueAsString(
                Map.of("accessToken", accessToken)
        );

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.setContentType("application/json");
        response.getWriter().write(json);
    }
}
