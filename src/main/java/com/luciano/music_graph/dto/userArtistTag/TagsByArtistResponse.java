package com.luciano.music_graph.dto.userArtistTag;

import com.luciano.music_graph.dto.ShortArtistInfoDto;
import com.luciano.music_graph.dto.userTag.TagResponse;

import java.util.List;

public record TagsByArtistResponse(
        ShortArtistInfoDto artist,
        List<TagResponse> tags
) {
}
