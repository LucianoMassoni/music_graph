package com.luciano.music_graph.dto;

import java.util.List;

public record DiscoveryGraphResponse(
        ShortArtistInfoDto artist,
        List<RelatedArtistNode> relatedArtistNodes
) {
}
