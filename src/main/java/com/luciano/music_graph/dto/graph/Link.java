package com.luciano.music_graph.dto.graph;

public record Link(
        String source,
        String target,
        int weight,
        boolean active
) {
}
