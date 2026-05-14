package com.luciano.music_graph.mapper;

import com.luciano.music_graph.dto.RegisterRequest;
import com.luciano.music_graph.dto.user.UserDto;
import com.luciano.music_graph.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    User toEntity(RegisterRequest request);

    UserDto toUserDto(User user);
}
