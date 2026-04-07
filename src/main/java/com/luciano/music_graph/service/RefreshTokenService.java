package com.luciano.music_graph.service;

import com.luciano.music_graph.exception.RefreshTokenExpirationException;
import com.luciano.music_graph.exception.RefreshTokenNotFoundException;
import com.luciano.music_graph.exception.UserNotFoundException;
import com.luciano.music_graph.model.RefreshToken;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.repository.RefreshTokenRepository;
import com.luciano.music_graph.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationMs;

    @Transactional
    public RefreshToken create(UUID userId){

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken rotate(RefreshToken oldToken){
        verifyExpiration(oldToken);

        User user = oldToken.getUser();
        refreshTokenRepository.revokeToken(oldToken.getToken());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public void verifyExpiration(RefreshToken token){

        if (token.getExpiresAt().isBefore(Instant.now())){
            refreshTokenRepository.delete(token);
            throw new RefreshTokenExpirationException();
        }
    }

    public RefreshToken findByToken(String token){

        return refreshTokenRepository.findByToken(token).orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found with token: " + token));
    }

    @Transactional
    public void revokeByRefreshToken(RefreshToken token){

        refreshTokenRepository.revokeToken(token.getToken());
    }
}
