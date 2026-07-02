package com.luciano.music_graph.dto;

import com.luciano.music_graph.dto.userTag.TagResponse;

import java.util.List;
import java.util.UUID;

public record UserArtistDetail(
        UUID id,
        String mbid,
        String name,
        String bio,
        List<ArtistTagData> tags,
        List<AlbumDetail> albums,
        List<TagResponse>  userTags,
        boolean followed
) {
}
