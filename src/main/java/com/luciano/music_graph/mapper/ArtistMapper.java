package com.luciano.music_graph.mapper;


import com.luciano.music_graph.dto.*;
import com.luciano.music_graph.dto.lastfm.LFArtist;
import com.luciano.music_graph.dto.lastfm.LFArtistInfo;
import com.luciano.music_graph.dto.lastfm.LFImageItem;
import com.luciano.music_graph.model.Artist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ArtistMapper {

    ArtistSearchData toArtistSearchData(LFArtist lfArtist);

    @Mapping(target = "bio", source = "bio.content")
    @Mapping(target = "imageUrl", expression = "java(extractImage(lfArtistInfo.image()))")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Artist toEntity(LFArtistInfo lfArtistInfo);

    default String extractImage(List<LFImageItem> images) {
        return images.stream()
                .filter(img -> "extralarge".equals(img.size()))
                .map(LFImageItem::text)
                .findFirst()
                .orElse(null);
    }


    @Mapping(target = "tags", source = "tagList")
    @Mapping(target = "albums", source = "albumList")
    ArtistDetail toArtistDetail(Artist artist, List<ArtistTagData> tagList, List<AlbumDetail> albumList);
}
