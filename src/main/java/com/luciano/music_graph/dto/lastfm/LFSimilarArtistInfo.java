package com.luciano.music_graph.dto.lastfm;

import java.util.List;

public record LFSimilarArtistInfo(
        String name,
        String mbid,
        String match
) {
}
