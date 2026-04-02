package com.luciano.music_graph.exception;

public class UserTagNameAlreadyExistsException extends RuntimeException {
    public UserTagNameAlreadyExistsException(String tag) {
        super("tag: " + tag + " already exists");
    }
}
