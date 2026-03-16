package com.luciano.music_graph.exception;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException(String email) {
        super("User not found with email: " + email);
    }
}
