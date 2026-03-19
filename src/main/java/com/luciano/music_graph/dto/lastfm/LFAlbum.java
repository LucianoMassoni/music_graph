package com.luciano.music_graph.dto.lastfm;

import java.util.List;

public record LFAlbum(
        String name,
        String mbid,
        String url,
        List<LFImageItem> image
) {
}
