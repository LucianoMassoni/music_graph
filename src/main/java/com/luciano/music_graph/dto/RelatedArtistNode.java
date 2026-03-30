package com.luciano.music_graph.dto;

public record RelatedArtistNode(
        String name,
        String mbid,
        Integer weight,
        boolean followed
) {
}
