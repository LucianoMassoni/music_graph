package com.luciano.music_graph.mapper;


import com.luciano.music_graph.dto.ApiArtistRelatedInfoDto;
import com.luciano.music_graph.dto.ArtistRelatedDto;
import com.luciano.music_graph.dto.ShortArtistInfoDto;
import com.luciano.music_graph.dto.lastfm.LFSimilarArtistInfo;
import com.luciano.music_graph.dto.lastfm.LFSimilarArtistResponse;
import com.luciano.music_graph.model.ApiArtistRelation;
import com.luciano.music_graph.model.Artist;
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

    ShortArtistInfoDto toShortArtistInfoDto(Artist artist);

    default ApiArtistRelatedInfoDto toRelatedDto(ApiArtistRelation relation, Artist mainArtist) {
        Artist related = relation.getArtistA().getId().equals(mainArtist.getId())
                ? relation.getArtistB()
                : relation.getArtistA();
        return new ApiArtistRelatedInfoDto(toShortArtistInfoDto(related), relation.getWeight());
    }
}
