package com.luciano.music_graph.mapper;

import com.luciano.music_graph.dto.ShortArtistInfoDto;
import com.luciano.music_graph.model.Artist;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GraphMapper {

    ShortArtistInfoDto toShortArtistInfoDto(Artist artist);
}
