package com.luciano.music_graph.exception;

public class UserArtistNotFoundException extends RuntimeException {
    public UserArtistNotFoundException(String message) {
        super(message);
    }
}
