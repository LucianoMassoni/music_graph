package com.luciano.music_graph.dto;

import java.util.List;

public record ApiArtistRelationResponse(
        ShortArtistInfoDto artist,
        List<ApiArtistRelatedInfoDto> relatedArtists
) {
}
