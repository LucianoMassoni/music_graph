package com.luciano.music_graph.mapper;


import com.luciano.music_graph.dto.*;
import com.luciano.music_graph.dto.lastfm.LFSimilarArtistInfo;
import com.luciano.music_graph.dto.lastfm.LFSimilarArtistResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApiArtistRelationMapper {

    @Mapping(target = "weight",  expression = "java(Double.parseDouble(info.match()))")
    ArtistRelatedDto toArtistRelatedDto(LFSimilarArtistInfo info);

    default List<ArtistRelatedDto> toArtistRelatedList(LFSimilarArtistResponse response){
        return response.similarartists().artist().stream()
                .map(this::toArtistRelatedDto)
                .toList();
    }
}
