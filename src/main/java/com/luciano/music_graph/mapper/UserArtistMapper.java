package com.luciano.music_graph.mapper;

import com.luciano.music_graph.dto.*;
import com.luciano.music_graph.dto.lastfm.LFArtist;
import com.luciano.music_graph.dto.userTag.TagResponse;
import com.luciano.music_graph.model.Artist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserArtistMapper {

    ShortArtistInfoDto toShortArtistInfoDto(Artist artist);

    default UserArtistResponse toUserArtistResponse(List<Artist> artists){
        return new UserArtistResponse(artists.stream().map(this::toShortArtistInfoDto).toList());
    }

    @Mapping(target = "name", source = "lfArtist.name")
    @Mapping(target = "id", source = "lfArtist.mbid")
    @Mapping(target = "followed", source = "followed")
    ArtistSearchData toArtistSearchData(LFArtist lfArtist, boolean followed);

    UserArtistDetail toUserArtistDetail(ArtistDetail artistDetail, List<TagResponse> userTags, boolean followed);
}
