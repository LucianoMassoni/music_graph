package com.luciano.music_graph.dto;

public record ArtistRelationship(
        String artistAMbid,
        String artistBMbid,
        Integer weight
) {
}
