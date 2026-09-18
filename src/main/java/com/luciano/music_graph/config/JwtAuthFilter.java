package com.luciano.music_graph.config;

import com.luciano.music_graph.dto.ExceptionHandlerDto;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.repository.UserRepository;
import com.luciano.music_graph.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/auth/");
    }


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String jwt;
        String userId;

        Cookie accessTokenCookie = WebUtils.getCookie(request, "accessToken");

        if (accessTokenCookie == null){
            filterChain.doFilter(request, response);
            return;
        }

        jwt = accessTokenCookie.getValue();

        try {
            userId = jwtService.extractUserId(jwt);
        } catch (Exception ex){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(
                    new ExceptionHandlerDto(401, "Unauthorized", "Missing or invalid token", Instant.now())
            ));
            return;
        }

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null){
            Optional<User> userOpt = userRepository.findById(UUID.fromString(userId));

            if (userOpt.isEmpty()){
                filterChain.doFilter(request, response);
                return;
            }
            User user = userOpt.get();

            if (jwtService.isTokenValid(jwt, user)){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
