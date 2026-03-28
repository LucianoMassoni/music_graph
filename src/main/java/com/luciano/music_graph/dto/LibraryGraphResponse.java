package com.luciano.music_graph.dto;

public record LibraryGraphResponse(
        ArtistNode artistNode,
        RelationEdge relationEdge
) {
}
