package com.luciano.music_graph.service;

import com.luciano.music_graph.BaseIntegrationTest;
import com.luciano.music_graph.exception.RefreshTokenExpirationException;
import com.luciano.music_graph.exception.RefreshTokenNotFoundException;
import com.luciano.music_graph.exception.UserNotFoundException;
import com.luciano.music_graph.model.RefreshToken;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.repository.RefreshTokenRepository;
import com.luciano.music_graph.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class RefreshTokenServiceTest extends BaseIntegrationTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private final Long EXPIRATION_MS = 5000L;

    @BeforeEach
    public void setUp(){

        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMs", EXPIRATION_MS);
    }



    @Test
    void create_shouldCreateCorrect(){

        UUID id = UUID.randomUUID();

        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken refreshToken = refreshTokenService.create(id);

        verify(userRepository).findById(id);
        verify(refreshTokenRepository).save(any(RefreshToken.class));

        assertNotNull(refreshToken);
        assertEquals(user, refreshToken.getUser());
        assertNotNull(refreshToken.getToken());
        assertNotNull(refreshToken.getExpiresAt());
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {

        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            refreshTokenService.create(id);
        });

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rotate_shouldRotateCorrectly(){

        User user = new User();
        user.setId(UUID.randomUUID());

        RefreshToken oldToken = RefreshToken.builder()
                .user(user)
                .token("old-token")
                .expiresAt(Instant.now().plusMillis(10000)) // válido
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken newToken = refreshTokenService.rotate(oldToken);

        verify(refreshTokenRepository).revokeToken("old-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));

        assertNotNull(newToken);
        assertEquals(user, newToken.getUser());
        assertNotEquals(oldToken.getToken(), newToken.getToken());
    }

    @Test
    void rotate_shouldThrowException(){

        User user = new User();
        user.setId(UUID.randomUUID());

        RefreshToken expiredToken = RefreshToken.builder()
                .user(user)
                .token("expired-token")
                .expiresAt(Instant.now().minusMillis(1000)) // expirado
                .build();

        assertThrows(RefreshTokenExpirationException.class, () -> {
            refreshTokenService.rotate(expiredToken);
        });

        verify(refreshTokenRepository, never()).save(any());
        verify(refreshTokenRepository, never()).revokeToken(any());
    }

    @Test
    void verifyExpiration_happyPath(){

        User user = new User();
        user.setId(UUID.randomUUID());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token("not_expired")
                .expiresAt(Instant.now().plusMillis(1000))
                .build();

        refreshTokenService.verifyExpiration(refreshToken);

        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void verifyExpiration_shouldDeleteAndThrowWhenExpired() {

        User user = new User();
        user.setId(UUID.randomUUID());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token("expired")
                .expiresAt(Instant.now().minusMillis(1000))
                .build();

        assertThrows(RefreshTokenExpirationException.class, () -> {
            refreshTokenService.verifyExpiration(refreshToken);
        });

        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void findByToken_correctly(){

        User user = new User();
        user.setId(UUID.randomUUID());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token("token")
                .expiresAt(Instant.now().minusMillis(1000))
                .build();

        when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(refreshToken));

        RefreshToken response = refreshTokenService.findByToken("token");

        verify(refreshTokenRepository).findByToken("token");

        assertNotNull(response);
        assertEquals(response, refreshToken);
    }

    @Test
    void findByToken_throwException(){

        when(refreshTokenRepository.findByToken("token"))
                .thenReturn(Optional.empty());

        assertThrows(RefreshTokenNotFoundException.class, () -> {
            refreshTokenService.findByToken("token");
        });
    }

    @Test
    void revokeByRefreshToken_shouldCallRepository() {

        RefreshToken token = new RefreshToken();
        token.setToken("abc");

        refreshTokenService.revokeByRefreshToken(token);

        verify(refreshTokenRepository).revokeToken("abc");
    }
}
