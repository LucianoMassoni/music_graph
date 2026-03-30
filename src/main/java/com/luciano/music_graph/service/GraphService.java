package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.*;
import com.luciano.music_graph.exception.ArtistNotFoundException;
import com.luciano.music_graph.mapper.GraphMapper;
import com.luciano.music_graph.model.ApiArtistRelation;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.model.UserArtist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GraphService {

    private final UserArtistService userArtistService;
    private final ApiArtistRelationService apiArtistRelationService;
    private final ArtistService artistService;
    private final GraphMapper mapper;

    public LibraryGraphResponse getLibraryGraph(User user){

        List<UserArtist> userArtists = userArtistService.getAllFollowedEntity(user);

        List<Artist> artists = userArtists.stream().map(UserArtist::getArtist).toList();

        RelationEdge relationEdge = apiArtistRelationService.getApiArtistRelations(artists);
        ArtistNode artistNode = userArtistService.toArtistNode(userArtists);

        return new LibraryGraphResponse(artistNode, relationEdge);
    }

    public DiscoveryGraphResponse getDiscoveryGraph(User user, String mbid, int limit){

        Artist artist = artistService.findByMbid(mbid).orElseThrow(() -> new ArtistNotFoundException(mbid));

        List<UserArtist> userArtists = userArtistService.getAllFollowedEntity(user);

        List<ApiArtistRelation> relatedArtist = apiArtistRelationService.getRelatedArtists(mbid, limit);

        List<RelatedArtistNode> relatedArtistNodes = new ArrayList<>();

        for (ApiArtistRelation related : relatedArtist){
            if (Objects.equals(artist, related.getArtistA())){
                relatedArtistNodes.add( new RelatedArtistNode(
                        related.getArtistB().getName(),
                        related.getArtistB().getMbid(),
                        related.getWeight(),
                        userArtists.stream().anyMatch(userArtist -> userArtist.getArtist().equals(related.getArtistB()))
                ));
            } else {
                relatedArtistNodes.add( new RelatedArtistNode(
                        related.getArtistA().getName(),
                        related.getArtistA().getMbid(),
                        related.getWeight(),
                        userArtists.stream().anyMatch(userArtist -> userArtist.getArtist().equals(related.getArtistA()))
                ));
            }
        }

        return new DiscoveryGraphResponse(mapper.toShortArtistInfoDto(artist), relatedArtistNodes);
    }
}
