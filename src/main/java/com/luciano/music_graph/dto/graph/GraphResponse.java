package com.luciano.music_graph.dto.graph;

import java.util.List;

public record GraphResponse(
        List<Node> nodes,
        List<Link> links
) {
}
