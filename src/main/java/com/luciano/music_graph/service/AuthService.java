package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.AuthTokens;
import com.luciano.music_graph.dto.LoginRequest;
import com.luciano.music_graph.dto.RegisterRequest;
import com.luciano.music_graph.model.RefreshToken;
import com.luciano.music_graph.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    public AuthTokens register(RegisterRequest request){
        User user = userService.create(request);

        RefreshToken refreshToken = refreshTokenService.create(user.getId());

        String accessToken = jwtService.generateToken(user);

        return new AuthTokens(refreshToken.getToken(), accessToken);
    }

    public AuthTokens login(LoginRequest request){

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userService.access(request);

        RefreshToken refreshToken = refreshTokenService.create(user.getId());

        String accessToken = jwtService.generateToken(user);

        return new AuthTokens(refreshToken.getToken(), accessToken);
    }

    public AuthTokens refresh(RefreshToken oldToken){

        User user = oldToken.getUser();

        RefreshToken refreshToken = refreshTokenService.rotate(oldToken);

        String accessToken = jwtService.generateToken(user);

        return new AuthTokens(refreshToken.getToken(), accessToken);
    }

    public void logout(RefreshToken oldToken){

        refreshTokenService.revokeByRefreshToken(oldToken);
    }
}
