package com.luciano.music_graph.dto;

import java.util.List;

public record ArtistSearchResult(
        List<ArtistSearchData> artist
) {
}
