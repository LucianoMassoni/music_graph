package com.luciano.music_graph.config;

import com.luciano.music_graph.dto.ExceptionHandlerDto;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.repository.UserRepository;
import com.luciano.music_graph.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        String userId;

        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(
                    new ExceptionHandlerDto(403, "Forbidden", "Access denied", Instant.now())
            ));
            return;
        }

        jwt = authHeader.substring(7); // quita el "Bearer ". de ahi el 7.

        try {
            userId = jwtService.extractUserId(jwt);
        } catch (ExpiredJwtException ex){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(
                    new ExceptionHandlerDto(401, "Unauthorized", "Missing or invalid token", Instant.now())
            ));
            return;
        }

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null){
            Optional<User> user = userRepository.findById(UUID.fromString(userId));

            if (jwtService.isTokenValid(jwt, user.get())){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user.get(),
                        null,
                        user.get().getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
