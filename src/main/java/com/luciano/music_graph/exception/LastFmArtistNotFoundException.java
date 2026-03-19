package com.luciano.music_graph.exception;

public class LastFmArtistNotFoundException extends RuntimeException {
    public LastFmArtistNotFoundException(String message) {
        super(message);
    }
}
