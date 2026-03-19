package com.luciano.music_graph.dto.lastfm;

import java.util.List;

public record LFArtistInfo(
        String name,
        String mbid,
        List<LFImageItem> image,
        LFTagWrapper tags,
        LFBio bio
) {}
