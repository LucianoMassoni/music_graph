package com.luciano.music_graph.dto;

import java.util.List;

public record ArtistNode(
        List<ShortArtistInfoDto> artistsNodes
) {
}
