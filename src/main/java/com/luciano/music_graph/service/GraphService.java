package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.*;
import com.luciano.music_graph.exception.ArtistNotFoundException;
import com.luciano.music_graph.mapper.GraphMapper;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.model.UserArtist;
import com.luciano.music_graph.repository.GraphRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GraphService {

    private final UserArtistService userArtistService;
    private final GraphRepository graphRepository;
    private final ArtistService artistService;
    private final GraphMapper mapper;

    public LibraryGraphResponse getLibraryGraph(User user){

        List<UserArtist> userArtists = userArtistService.getAllFollowedEntity(user);

        List<Object[]> combinedRelations = graphRepository.getCombinedRelationsForLibrary(user.getId());

        List<ArtistRelationship> relationships = new ArrayList<>();

        for (Object[] row : combinedRelations){
            relationships.add(new ArtistRelationship(
                    row[0].toString(),
                    row[1].toString(),
                    ((Number) row[2]).intValue()
            ));
        }

        RelationEdge relationEdge = new RelationEdge(relationships);

        ArtistNode artistNode = userArtistService.toArtistNode(userArtists);

        return new LibraryGraphResponse(artistNode, relationEdge);
    }

    public DiscoveryGraphResponse getDiscoveryGraph(User user, String mbid, int limit){

        // traigo el artista seleccionado de la búsqueda
        Artist artist = artistService.findByMbid(mbid).orElseThrow(() -> new ArtistNotFoundException(mbid));

        // traigo a los artistas seguidos del usuario
        List<UserArtist> userArtists = userArtistService.getAllFollowedEntity(user);

        // traigo a los artistas relacionados del primer artista
        List<Object[]> combinedRelatedArtists = graphRepository.getCombinedRelationsForDiscovery(user.getId(), mbid, PageRequest.of(0, limit));

        // creo una lista de RelatedArtistNode, que dependiendo de su peso será la cercanía.
        List<RelatedArtistNode> relatedArtistNodes = new ArrayList<>();

        for (Object[] row : combinedRelatedArtists){
            String artistAMbid = row[0].toString();
            String artistAName = row[1].toString();
            String artistBMbid = row[2].toString();
            String artistBName = row[3].toString();
            Integer weight = ((Number) row[4]).intValue();

            if (artist.getMbid().equals(artistAMbid)){
                relatedArtistNodes.add(new RelatedArtistNode(
                        artistBName,
                        artistBMbid,
                        weight,
                        userArtists.stream().anyMatch(userArtist -> userArtist.getArtist().getMbid().equals(artistBMbid))
                ));
            } else {
                relatedArtistNodes.add(new RelatedArtistNode(
                        artistAName,
                        artistAMbid,
                        weight,
                        userArtists.stream().anyMatch(userArtist -> userArtist.getArtist().getMbid().equals(artistAMbid))
                ));
            }
        }

        return new DiscoveryGraphResponse(mapper.toShortArtistInfoDto(artist), relatedArtistNodes);
    }
}
