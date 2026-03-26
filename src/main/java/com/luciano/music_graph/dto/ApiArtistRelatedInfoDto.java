package com.luciano.music_graph.dto;

public record ApiArtistRelatedInfoDto(
        ShortArtistInfoDto artist,
        Integer weight
) {
}
