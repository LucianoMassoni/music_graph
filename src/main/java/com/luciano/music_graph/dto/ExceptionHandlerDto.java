package com.luciano.music_graph.dto;

import java.time.Instant;

public record ExceptionHandlerDto(
        int status,
        String error,
        String message,
        Instant timestamp
) {
}
