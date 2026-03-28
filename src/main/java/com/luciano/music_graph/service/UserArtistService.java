package com.luciano.music_graph.service;

import com.luciano.music_graph.client.LastFmClient;
import com.luciano.music_graph.dto.ApiArtistRelationResponse;
import com.luciano.music_graph.dto.ArtistNode;
import com.luciano.music_graph.dto.UserArtistResponse;
import com.luciano.music_graph.exception.ArtistNotFoundException;
import com.luciano.music_graph.exception.UserArtistNotFoundException;
import com.luciano.music_graph.mapper.UserArtistMapper;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.model.UserArtist;
import com.luciano.music_graph.repository.UserArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserArtistService {

    private final UserArtistRepository userArtistRepository;
    private final ArtistService artistService;
    private final ApiArtistRelationService apiArtistRelationService;
    private final LastFmClient lastFmClient;
    private final UserArtistMapper mapper;


    @Lazy
    private UserArtist saveUserArtist(User user, Artist artist){

        return userArtistRepository.save(
                new UserArtist(
                        null,
                        user,
                        artist,
                        true,
                        Instant.now()
                )
        );
    }

    public ApiArtistRelationResponse followArtist(User user, String mbid){

        Artist artist = artistService.findByMbid(mbid).orElseThrow(() -> new ArtistNotFoundException(mbid));

        UserArtist userArtist = userArtistRepository.findByUserAndArtist(user, artist).orElseGet(() -> saveUserArtist(user, artist));

        if (!userArtist.isFollowed()){
            userArtist.setFollowed(true);
            userArtistRepository.save(userArtist);
        }

        return apiArtistRelationService.buidApiRelations(artist, lastFmClient.getSimilar(mbid));
    }


    public void unfollowArtist(User user, String mbid){

        Artist artist = artistService.findByMbid(mbid).orElseThrow(() -> new ArtistNotFoundException(mbid));

        UserArtist userArtist = userArtistRepository.findByUserAndArtist(user, artist).orElseThrow(() -> new UserArtistNotFoundException(""));

        if (userArtist.isFollowed()){
            userArtist.setFollowed(false);
            userArtistRepository.save(userArtist);
        }
    }

    public UserArtistResponse getAllFollowed(User user){
        List<UserArtist> userArtistList = userArtistRepository.getAllFollowedByUser(user).stream().filter(UserArtist::isFollowed).toList();

        return mapper.toUserArtistResponse(userArtistList.stream().map(UserArtist::getArtist).toList());
    }

    public List<UserArtist> getAllFollowedEntity(User user){

        return userArtistRepository.getAllFollowedByUser(user).stream().filter(UserArtist::isFollowed).toList();
    }

    public ArtistNode toArtistNode(List<UserArtist> userArtistList){

        return mapper.toArtistNode(userArtistList.stream().map(UserArtist::getArtist).toList());
    }
}
