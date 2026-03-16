package com.luciano.music_graph.mapper;

import com.luciano.music_graph.dto.RegisterRequest;
import com.luciano.music_graph.model.Role;
import com.luciano.music_graph.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password", ignore = true)
    User toEntity(RegisterRequest request);
}
