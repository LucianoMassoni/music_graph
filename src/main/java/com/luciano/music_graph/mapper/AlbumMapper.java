package com.luciano.music_graph.mapper;

import com.luciano.music_graph.dto.AlbumDetail;
import com.luciano.music_graph.dto.lastfm.LFAlbum;
import com.luciano.music_graph.dto.lastfm.LFImageItem;
import com.luciano.music_graph.model.Album;
import com.luciano.music_graph.model.Artist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AlbumMapper {

    AlbumDetail toAlbumData(Album album);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mbid", source = "album.mbid")
    @Mapping(target = "artist", source = "artist")
    @Mapping(target = "lastfmUrl", source = "album.url")
    @Mapping(target = "imageUrl", expression = "java(extractImage(album.image()))")
    @Mapping(target = "name", source = "album.name")
    Album toEntity(LFAlbum album, Artist artist);

    default String extractImage(List<LFImageItem> images) {
        return images.stream()
                .filter(img -> "extralarge".equals(img.size()))
                .map(LFImageItem::text)
                .findFirst()
                .orElse(null);
    }
}
