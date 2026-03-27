package com.luciano.music_graph.mapper;

import com.luciano.music_graph.dto.ShortArtistInfoDto;
import com.luciano.music_graph.dto.UserArtistResponse;
import com.luciano.music_graph.model.Artist;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserArtistMapper {

    ShortArtistInfoDto toShortArtistInfoDto(Artist artist);

    default UserArtistResponse toUserArtistResponse(List<Artist> artists){
        return new UserArtistResponse(artists.stream().map(this::toShortArtistInfoDto).toList());
    }

}
