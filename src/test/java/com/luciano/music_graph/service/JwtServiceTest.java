package com.luciano.music_graph.service;

import com.luciano.music_graph.model.Role;
import com.luciano.music_graph.model.User;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private final String SECRET_KEY = "YXNkZmFzZGZhc2RmYXNkZmFzZGZhc2RmYXNkZmFzZGZhc2RmYXNkZmdnZ2U=";
    private final Long EXPIRATION_MS = 2000L;

    private JwtService jwtService;

    @BeforeEach
    public void startUp(){
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "expirationMs", EXPIRATION_MS);
    }

    private User createUser(){
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole(Role.USER);

        return user;
    }

    @Test
    void shouldGenerateTokenWithCorrectClaims(){
        User user = createUser();

        String token = jwtService.generateToken(user);
        Claims claims = jwtService.extractAllClaims(token);

        assertNotNull(token);
        assertEquals(user.getId().toString(), claims.getSubject());
        assertEquals(user.getRole(), Role.valueOf(claims.get("role").toString()));
    }

    @Test
    public void extractUserId_shouldExtractTheUserId(){
        User user = createUser();

        String token = jwtService.generateToken(user);
        String response = jwtService.extractUserId(token);

        assertEquals(user.getId().toString(), response);
    }

    @Test
    void shouldReturnFalseWhenUserIdDoesNotMatch() {
        User user1 = createUser();
        User user2 = createUser();

        String token = jwtService.generateToken(user1);

        boolean isValid = jwtService.isTokenValid(token, user2);

        assertFalse(isValid);
    }

    @Test
    void shouldValidateCorrectToken() {
        User user = createUser();

        String token = jwtService.generateToken(user);
        boolean isValid = jwtService.isTokenValid(token, user);

        assertTrue(isValid);
    }

    @Test
    void shouldThrowExceptionForMalformedToken() {
        String invalidToken = "esto.no.es.un.jwt";

        assertThrows(Exception.class, () -> {
            jwtService.extractUserId(invalidToken);
        });
    }

    @Test
    void shouldReturnFalseWhenTokenIsExpired() throws InterruptedException {
        User user = createUser();

        String token = jwtService.generateToken(user);

        Thread.sleep(5000);

        boolean isValid = jwtService.isTokenValid(token, user);

        assertFalse(isValid);
    }
}
