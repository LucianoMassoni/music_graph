package com.luciano.music_graph.dto;

import java.util.UUID;

public record ShortArtistInfoDto(
        UUID id,
        String name,
        String mbid
) {
}