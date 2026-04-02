package com.luciano.music_graph.dto.userArtistTag;

import java.util.UUID;

public record UserArtistTagResponse(
        String artistMbid,
        UUID tagId,
        String tagName
) {
}
