package com.luciano.music_graph.dto.userArtistTag;

import java.util.UUID;

public record UserArtistTagResponse(
        UUID id,
        String name
) {
}
