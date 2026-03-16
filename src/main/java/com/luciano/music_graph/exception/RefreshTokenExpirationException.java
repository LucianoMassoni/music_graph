package com.luciano.music_graph.exception;

public class RefreshTokenExpirationException extends RuntimeException {
    public RefreshTokenExpirationException() {
        super("Refresh token has been expired.");
    }
}
