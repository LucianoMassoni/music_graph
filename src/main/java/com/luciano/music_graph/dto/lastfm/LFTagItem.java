package com.luciano.music_graph.dto.lastfm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LFTagItem(
        @JsonProperty("#text") String text,
        String size
) {
}
