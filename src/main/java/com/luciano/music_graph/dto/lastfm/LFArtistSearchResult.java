package com.luciano.music_graph.dto.lastfm;

import java.util.List;

public record LFArtistSearchResult(
        List<LFArtist> artist
) {
}
