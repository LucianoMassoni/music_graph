package com.luciano.music_graph.exception;

public class ArtistNotFoundException extends RuntimeException {
    public ArtistNotFoundException(String mbid) {
        super("Artist not found with mbid: " + mbid);
    }
}
