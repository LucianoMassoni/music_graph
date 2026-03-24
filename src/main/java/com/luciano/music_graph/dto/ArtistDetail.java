package com.luciano.music_graph.dto;

import java.util.List;
import java.util.UUID;

public record ArtistDetail(
        UUID id,
        String mbid,
        String name,
        String bio,
        List<ArtistTagData> tags,
        List<AlbumDetail> albums
) {
}
