package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.*;
import com.luciano.music_graph.dto.graph.GraphResponse;
import com.luciano.music_graph.dto.graph.Link;
import com.luciano.music_graph.dto.graph.Node;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.repository.GraphRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GraphService {

    private final UserArtistService userArtistService;
    private final GraphRepository graphRepository;
    private final UserArtistRelationService userArtistRelationService;

    public GraphResponse getLibraryGraph(User user){

        List<Object[]> combinedRelations = graphRepository.getCombinedRelationsForLibrary(user.getId());
        List<Link> links = new ArrayList<>();

        for (Object[] row : combinedRelations){
            String name = row[0].toString();
            String mbid = row[1].toString();
            int weight = ((Number) row[2]).intValue();

            links.add(new Link(
                    name,
                    mbid,
                    weight,
                    true
            ));
        }

        List<Node> nodes = userArtistService.getFollowedNodes(user);

        return new GraphResponse(nodes, links);
    }

    public GraphResponse getDiscoveryGraph(User user, String mbid){

        List<Node> nodes = userArtistService.getArtistsAndSimilar(user, mbid);
        List<Link> links = userArtistRelationService.getLinks(user, mbid);

        return new GraphResponse(nodes, links);
    }
}
