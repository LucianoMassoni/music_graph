package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.userTag.TagRequest;
import com.luciano.music_graph.dto.userTag.TagResponse;
import com.luciano.music_graph.exception.UserTagNameAlreadyExistsException;
import com.luciano.music_graph.exception.UserTagNotFoundException;
import com.luciano.music_graph.mapper.UserTagMapper;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.model.UserTag;
import com.luciano.music_graph.repository.UserTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserTagService {

    private final UserTagRepository userTagRepository;
    private final UserTagMapper mapper;

    public TagResponse create(User user, TagRequest request){

        if (tagNameExists(user, request.name())) throw new UserTagNameAlreadyExistsException(request.name());

        UserTag userTag = mapper.toEntity(user, request);
        userTag = userTagRepository.save(userTag);

        return mapper.toTagResponse(userTag);
    }

    private boolean tagNameExists(User user, String name){

        return !userTagRepository.findByUserTagName(user, name).isEmpty();
    }

    public List<TagResponse> getAllUserTags(User user){

        return userTagRepository.findByUser(user).stream().map(mapper::toTagResponse).toList();
    }

    public void deleteTag(UUID id){

        userTagRepository.deleteById(id);
    }

    public UserTag getEntityById(UUID id){

        return userTagRepository.findById(id).orElseThrow(() -> new UserTagNotFoundException(id));
    }
}
