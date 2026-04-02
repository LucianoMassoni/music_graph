package com.luciano.music_graph.mapper;

import com.luciano.music_graph.dto.userTag.TagRequest;
import com.luciano.music_graph.dto.userTag.TagResponse;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.model.UserTag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserTagMapper {

    TagResponse toTagResponse(UserTag userTag);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "user", source = "user")
    UserTag toEntity(User user, TagRequest request);
}
