package com.luciano.music_graph.mapper;


import com.luciano.music_graph.dto.ShortArtistInfoDto;
import com.luciano.music_graph.dto.userArtistTag.ArtistsByTagResponse;
import com.luciano.music_graph.dto.userArtistTag.TagsByArtistResponse;
import com.luciano.music_graph.dto.userArtistTag.UserArtistTagResponse;
import com.luciano.music_graph.dto.userTag.TagResponse;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.model.UserArtistTag;
import com.luciano.music_graph.model.UserTag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserArtistTagMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "artist", source = "artist")
    @Mapping(target = "userTag", source = "userTag")
    UserArtistTag toEntity(User user, Artist artist, UserTag userTag);

    @Mapping(target = "artistMbid", source = "userArtistTag.artist.mbid")
    @Mapping(target = "tagId", source = "userArtistTag.userTag.id")
    @Mapping(target = "tagName", source = "userArtistTag.userTag.name")
    UserArtistTagResponse toUserArtistTagResponse(UserArtistTag userArtistTag);

    TagResponse toTagResponse(UserTag userTag);
    ShortArtistInfoDto toShortArtistInfoDto(Artist artist);

    default ArtistsByTagResponse toArtistsByTag(UserTag userTag, List<Artist> artists){
        return new ArtistsByTagResponse(
                toTagResponse(userTag),
                artists.stream().map(this::toShortArtistInfoDto).toList()
        );
    }

    default TagsByArtistResponse toTagsByArtist(Artist artist, List<UserTag> tags){
        return new TagsByArtistResponse(
                toShortArtistInfoDto(artist),
                tags.stream().map(this::toTagResponse).toList()
        );
    }
}
