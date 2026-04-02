package com.luciano.music_graph.service;


import com.luciano.music_graph.dto.userArtistTag.ArtistsByTagResponse;
import com.luciano.music_graph.dto.userArtistTag.TagsByArtistResponse;
import com.luciano.music_graph.dto.userArtistTag.UserArtistTagResponse;
import com.luciano.music_graph.exception.ArtistNotFoundException;
import com.luciano.music_graph.exception.UserArtistTagNotFoundException;
import com.luciano.music_graph.mapper.UserArtistTagMapper;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.model.UserArtistTag;
import com.luciano.music_graph.model.UserTag;
import com.luciano.music_graph.repository.UserArtistTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserArtistTagService {

    private final ArtistService artistService;
    private final UserTagService userTagService;
    private final UserArtistTagRepository userArtistTagRepository;
    private final UserArtistTagMapper mapper;

    public UserArtistTagResponse create(User user, UUID tagId, String artistMbid){

        Artist artist = artistService.findByMbid(artistMbid).orElseThrow(() -> new ArtistNotFoundException(artistMbid));
        UserTag userTag = userTagService.getEntityById(tagId);


        UserArtistTag userArtistTag = mapper.toEntity(user, artist, userTag);
        userArtistTag = userArtistTagRepository.save(userArtistTag);

        return mapper.toUserArtistTagResponse(userArtistTag);
    }

    public void delete(UUID tagId, String artistMbid){

        UserArtistTag userArtistTag = userArtistTagRepository.findByTagIdAndArtistMbid(tagId, artistMbid).orElseThrow(() -> new UserArtistTagNotFoundException(
                "UserArtistTag not found with tagId: " + tagId + " and artist mbid: " + artistMbid
        ));

        userArtistTagRepository.delete(userArtistTag);
    }

    public List<Artist> getArtistsEntityByTagId(UUID tagId){

        return userArtistTagRepository.findArtistByTagId(tagId);
    }

    public ArtistsByTagResponse getArtistsByTagId(UUID tagId){

        UserTag userTag = userTagService.getEntityById(tagId);
        List<Artist> artists = userArtistTagRepository.findArtistByTagId(tagId);
        return mapper.toArtistsByTag(userTag, artists);
    }


    public TagsByArtistResponse getTagsByArtistMbid(String mbid){

        Artist artist = artistService.findByMbid(mbid).orElseThrow(() -> new ArtistNotFoundException(mbid));
        List<UserTag> userTags = userArtistTagRepository.findByArtistMbid(mbid);

        return mapper.toTagsByArtist(artist, userTags);
    }
}
