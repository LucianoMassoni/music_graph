package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.*;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.model.UserArtist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GraphService {

    private final UserArtistService userArtistService;
    private final ApiArtistRelationService apiArtistRelationService;

    public LibraryGraphResponse getLibraryGraph(User user){

        List<UserArtist> userArtists = userArtistService.getAllFollowedEntity(user);

        List<Artist> artists = userArtists.stream().map(UserArtist::getArtist).toList();

        RelationEdge relationEdge = apiArtistRelationService.getApiArtistRelations(artists);
        ArtistNode artistNode = userArtistService.toArtistNode(userArtists);

        return new LibraryGraphResponse(artistNode, relationEdge);
    }
}
