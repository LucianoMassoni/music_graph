package com.luciano.music_graph.dto;

import java.util.List;

public record UserArtistResponse(
        List<ShortArtistInfoDto> artist
) {
}
