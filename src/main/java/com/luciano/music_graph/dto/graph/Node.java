package com.luciano.music_graph.dto.graph;

public record Node(
        String name,
        String id,
        boolean followed
) {
}
