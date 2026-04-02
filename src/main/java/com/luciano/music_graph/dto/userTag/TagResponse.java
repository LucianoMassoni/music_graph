package com.luciano.music_graph.dto.userTag;

import java.util.UUID;

public record TagResponse(
        UUID id,
        String name
) {
}
