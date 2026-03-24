package com.luciano.music_graph.dto;

public record AlbumDetail(
        String name,
        int year,
        String imageUrl,
        String lastfmUrl
) {
}
