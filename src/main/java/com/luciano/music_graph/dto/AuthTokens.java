package com.luciano.music_graph.dto;

public record AuthTokens(
        String refreshToken,
        String accessToken
) {
}
