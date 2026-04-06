package com.luciano.music_graph.mapper;


import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.model.UserArtistRelation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserArtistRelationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "artistA", source = "artistA")
    @Mapping(target = "artistB", source = "artistB")
    @Mapping(target = "weight", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    UserArtistRelation toEntity(User user, Artist artistA, Artist artistB);
}
