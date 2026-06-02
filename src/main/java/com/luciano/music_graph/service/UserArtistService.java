package com.luciano.music_graph.service;

import com.luciano.music_graph.client.LastFmClient;
import com.luciano.music_graph.dto.*;
import com.luciano.music_graph.dto.graph.Node;
import com.luciano.music_graph.dto.lastfm.LFSearchResponse;
import com.luciano.music_graph.dto.lastfm.LFSimilarArtistResponse;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public void followArtist(User user, String mbid){

        Artist artist = artistService.findByMbid(mbid).orElseThrow(() -> new ArtistNotFoundException(mbid));

        UserArtist userArtist = userArtistRepository.findByUserAndArtist(user, artist).orElseGet(() -> saveUserArtist(user, artist));

        if (!userArtist.isFollowed()){
            userArtist.setFollowed(true);
            userArtistRepository.save(userArtist);
        }
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

    private boolean isFollowed(User user, String mbid){

        return userArtistRepository.isFollowed(user, mbid).orElse(false);
    }

    public ArtistSearchResult search(User user, String name){

        LFSearchResponse response = lastFmClient.search(name);

        Set<String> seen = new HashSet<>();

        List<ArtistSearchData> lista = response
                .results()
                .artistmatches()
                .artist().stream()
                .filter(artist -> !artist.mbid().isEmpty()) // Last.fm puede devolver artistas duplicados o sin MBID
                .filter(artist -> seen.add(artist.mbid()))
                .map(artist -> mapper.toArtistSearchData(artist, this.isFollowed(user, artist.mbid())))
                .toList();

        return new ArtistSearchResult(lista);
    }

    public List<Node> getArtistsAndSimilar(User user, String mbid){

        Artist artist = artistService.findByMbid(mbid).orElseThrow(() -> new ArtistNotFoundException(mbid));
        LFSimilarArtistResponse similar = lastFmClient.getSimilar(mbid);

        UserArtist userArtist = userArtistRepository.findByUserAndArtist(user, artist).orElse(null);

        // crea las relaciones y persiste en db.
        apiArtistRelationService.buildApiRelations(artist, similar);

        List<Node> nodes = getNodes(userArtistRepository.getAllNodesByUserAndArtist(user, artist));

        boolean followed = userArtist != null && userArtist.isFollowed();

        nodes.addFirst(new Node(
                artist.getName(),
                artist.getMbid(),
                followed
        ));

        return nodes;
    }

    private List<Node> getNodes(List<Object[]> response){

        List<Node> nodeList = new ArrayList<>();

        for (Object[] row : response){
            String name = row[0].toString();
            String mbid = row[1].toString();
            boolean followed = (boolean) row[2];

            nodeList.add(new Node(
                    name,
                    mbid,
                    followed
            ));
        }

        return nodeList;
    }

    public List<Node> getFollowedNodes(User user){
        List<UserArtist> userArtistList = userArtistRepository.getAllFollowedByUser(user).stream().filter(UserArtist::isFollowed).toList();

        List<Node> nodes = new ArrayList<>();

        userArtistList.forEach(userArtist -> {
            nodes.add(new Node(
                    userArtist.getArtist().getName(),
                    userArtist.getArtist().getMbid(),
                    userArtist.isFollowed()
            ));
        });

        return nodes;
    }
}
