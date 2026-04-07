package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.AuthTokens;
import com.luciano.music_graph.dto.LoginRequest;
import com.luciano.music_graph.dto.RegisterRequest;
import com.luciano.music_graph.exception.RefreshTokenExpirationException;
import com.luciano.music_graph.exception.UserAlreadyExistsException;
import com.luciano.music_graph.model.RefreshToken;
import com.luciano.music_graph.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest  {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    AuthenticationManager authenticationManager;


    @Test
    void register_correct(){

        User user = new User();;
        user.setId(UUID.randomUUID());
        user.setEmail("email@email.com");
        user.setUsername("username");

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token("refresh_token")
                .expiresAt(Instant.now().minusMillis(1000))
                .build();

        String accessToken = "access_token";


        when(userService.create(any())).thenReturn(user);
        when(refreshTokenService.create(any())).thenReturn(refreshToken);
        when(jwtService.generateToken(any())).thenReturn(accessToken);

        RegisterRequest request = new RegisterRequest(
                "username",
                "email@email.com",
                "password"
        );

        AuthTokens authTokens = authService.register(request);

        verify(userService).create(request);
        verify(refreshTokenService).create(user.getId());
        verify(jwtService).generateToken(user);


        assertEquals(refreshToken.getToken(), authTokens.refreshToken());
        assertEquals(accessToken, authTokens.accessToken());
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {

        RegisterRequest request = new RegisterRequest(
                "username",
                "email@email.com",
                "password"
        );

        when(userService.create(request))
                .thenThrow(new UserAlreadyExistsException(request.email()));

        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(request);
        });

        verify(refreshTokenService, never()).create(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_shouldThrowWhenUserAlreadyExists() {

        RegisterRequest request = new RegisterRequest("user", "email", "pass");

        when(userService.create(request))
                .thenThrow(new RuntimeException()); // o tu excepción real

        assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        });

        verify(refreshTokenService, never()).create(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldReturnTokensWhenCredentialsAreValid() {

        LoginRequest request = new LoginRequest("email@email.com", "password");

        User user = new User();
        user.setId(UUID.randomUUID());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token("refresh")
                .expiresAt(Instant.now().plusSeconds(10))
                .build();

        when(userService.access(request)).thenReturn(user);
        when(refreshTokenService.create(user.getId())).thenReturn(refreshToken);
        when(jwtService.generateToken(user)).thenReturn("access");

        AuthTokens tokens = authService.login(request);

        verify(authenticationManager).authenticate(any());
        verify(userService).access(request);

        assertEquals("refresh", tokens.refreshToken());
        assertEquals("access", tokens.accessToken());
    }


    @Test
    void login_shouldThrowWhenAuthenticationFails() {

        LoginRequest request = new LoginRequest("email", "wrong");

        doThrow(new RuntimeException())
                .when(authenticationManager)
                .authenticate(any());

        assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        verify(userService, never()).access(any());
        verify(refreshTokenService, never()).create(any());
    }

    @Test
    void login_shouldThrowWhenUserAccessFails() {

        LoginRequest request = new LoginRequest("email", "pass");

        when(authenticationManager.authenticate(any()))
                .thenReturn(mock(Authentication.class));

        when(userService.access(request))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        verify(refreshTokenService, never()).create(any());
    }

    @Test
    void login_shouldThrowWhenRefreshTokenFails() {

        LoginRequest request = new LoginRequest("email", "pass");

        User user = new User();
        user.setId(UUID.randomUUID());

        when(authenticationManager.authenticate(any()))
                .thenReturn(mock(Authentication.class));
        when(userService.access(request)).thenReturn(user);

        when(refreshTokenService.create(user.getId()))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void refresh_shouldRotateTokenAndGenerateNewAccessToken() {

        User user = new User();
        user.setId(UUID.randomUUID());

        RefreshToken oldToken = RefreshToken.builder()
                .user(user)
                .token("old")
                .expiresAt(Instant.now().plusSeconds(10))
                .build();

        RefreshToken newToken = RefreshToken.builder()
                .user(user)
                .token("new")
                .expiresAt(Instant.now().plusSeconds(10))
                .build();

        when(refreshTokenService.rotate(oldToken)).thenReturn(newToken);
        when(jwtService.generateToken(user)).thenReturn("access");

        AuthTokens tokens = authService.refresh(oldToken);

        verify(refreshTokenService).rotate(oldToken);
        verify(jwtService).generateToken(user);

        assertEquals("new", tokens.refreshToken());
        assertEquals("access", tokens.accessToken());
    }

    @Test
    void refresh_shouldThrowWhenTokenInvalid() {

        RefreshToken oldToken = new RefreshToken();

        when(refreshTokenService.rotate(oldToken))
                .thenThrow(new RefreshTokenExpirationException());

        assertThrows(RefreshTokenExpirationException.class, () -> {
            authService.refresh(oldToken);
        });

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void refresh_shouldThrowWhenRotateFails() {

        RefreshToken token = new RefreshToken();

        when(refreshTokenService.rotate(token))
                .thenThrow(new RefreshTokenExpirationException());

        assertThrows(RefreshTokenExpirationException.class, () -> {
            authService.refresh(token);
        });

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void refresh_shouldThrowWhenJwtFails() {

        User user = new User();
        user.setId(UUID.randomUUID());

        RefreshToken oldToken = RefreshToken.builder().user(user).build();
        RefreshToken newToken = RefreshToken.builder().user(user).token("new").build();

        when(refreshTokenService.rotate(oldToken)).thenReturn(newToken);
        when(jwtService.generateToken(user)).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> {
            authService.refresh(oldToken);
        });
    }


    @Test
    void logout_shouldRevokeToken() {

        RefreshToken token = new RefreshToken();
        token.setToken("abc");

        authService.logout(token);

        verify(refreshTokenService).revokeByRefreshToken(token);
    }

    @Test
    void logout_shouldPropagateException() {

        RefreshToken token = new RefreshToken();

        doThrow(new RuntimeException())
                .when(refreshTokenService)
                .revokeByRefreshToken(token);

        assertThrows(RuntimeException.class, () -> {
            authService.logout(token);
        });
    }
}
