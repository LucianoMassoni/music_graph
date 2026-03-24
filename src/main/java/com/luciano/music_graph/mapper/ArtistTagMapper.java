package com.luciano.music_graph.mapper;

import com.luciano.music_graph.dto.ArtistTagData;
import com.luciano.music_graph.dto.lastfm.LFTag;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.ArtistTag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArtistTagMapper {

    @Mapping(target = "name", source = "tag")
    ArtistTagData toArtistTagData(ArtistTag artistTag);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tag", source = "tag.name")
    ArtistTag toEntity(LFTag tag, Artist artist);
}
