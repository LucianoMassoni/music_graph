package com.luciano.music_graph.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("User already exists with username: " + username);
    }
}
