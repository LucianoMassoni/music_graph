package com.luciano.music_graph.dto;

public record ArtistRelatedDto(
        String name,
        String mbid,
        double weight
) {
}
