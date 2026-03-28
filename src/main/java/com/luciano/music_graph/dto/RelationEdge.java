package com.luciano.music_graph.dto;

import java.util.List;

public record RelationEdge(
        List<ArtistRelationship> edges
) {
}
