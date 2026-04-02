package com.luciano.music_graph.exception;

import java.util.UUID;

public class UserTagNotFoundException extends RuntimeException {
  public UserTagNotFoundException(UUID id) {
    super("UserTag not found with id: " + id);
  }
}
