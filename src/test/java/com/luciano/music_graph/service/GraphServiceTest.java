package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.*;
import com.luciano.music_graph.dto.graph.GraphResponse;
import com.luciano.music_graph.dto.graph.Link;
import com.luciano.music_graph.dto.graph.Node;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.repository.GraphRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphServiceTest {

    @InjectMocks
    private GraphService graphService;

    @Mock
    private UserArtistService userArtistService;

    @Mock
    private GraphRepository graphRepository;

    @Mock
    private UserArtistRelationService userArtistRelationService;


    @Test
    void should_build_library_graph_correctly() {

        User user = new User();
        user.setId(UUID.randomUUID());

        List<Object[]> dbResponse = List.of(
                new Object[]{"Artist A", "mbid-a", 80},
                new Object[]{"Artist B", "mbid-b", 60}
        );

        when(graphRepository.getCombinedRelationsForLibrary(user.getId()))
                .thenReturn(dbResponse);

        List<Node> nodes = List.of(
                new Node("User Artist", "mbid-user", true)
        );

        when(userArtistService.getFollowedNodes(user)).thenReturn(nodes);

        GraphResponse response = graphService.getLibraryGraph(user);

        // nodes
        assertEquals(1, response.nodes().size());
        assertEquals("User Artist", response.nodes().getFirst().name());

        // links
        assertEquals(2, response.links().size());
        assertEquals("Artist A", response.links().getFirst().source());
        assertEquals(80, response.links().getFirst().weight());
        assertTrue(response.links().getFirst().active());

        verify(graphRepository).getCombinedRelationsForLibrary(user.getId());
        verify(userArtistService).getFollowedNodes(user);
    }

    @Test
    void should_handle_empty_library_graph() {

        User user = new User();
        user.setId(UUID.randomUUID());

        when(graphRepository.getCombinedRelationsForLibrary(user.getId()))
                .thenReturn(List.of());

        when(userArtistService.getFollowedNodes(user))
                .thenReturn(List.of());

        GraphResponse response = graphService.getLibraryGraph(user);

        assertTrue(response.nodes().isEmpty());
        assertTrue(response.links().isEmpty());
    }

    @Test
    void should_fail_library_graph_when_repository_breaks() {

        User user = new User();
        user.setId(UUID.randomUUID());

        when(graphRepository.getCombinedRelationsForLibrary(user.getId()))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> graphService.getLibraryGraph(user));

        verify(userArtistService, never()).getFollowedNodes(any());
    }


    @Test
    void should_build_discovery_graph_correctly() {

        User user = new User();
        user.setId(UUID.randomUUID());

        String mbid = "mbid-artist";

        List<Node> nodes = List.of(
                new Node("A", "1", true),
                new Node("B", "2", false)
        );

        List<Link> links = List.of(
                new Link("A", "1", 50, true)
        );

        when(userArtistService.getArtistsAndSimilar(user, mbid))
                .thenReturn(nodes);

        when(userArtistRelationService.getLinks(user, mbid))
                .thenReturn(links);

        GraphResponse response = graphService.getDiscoveryGraph(user, mbid);

        assertEquals(2, response.nodes().size());
        assertEquals(1, response.links().size());

        assertEquals("A", response.links().getFirst().source());
        assertEquals(50, response.links().getFirst().weight());

        verify(userArtistService).getArtistsAndSimilar(user, mbid);
        verify(userArtistRelationService).getLinks(user, mbid);
    }

    @Test
    void should_handle_empty_discovery_graph() {

        User user = new User();
        user.setId(UUID.randomUUID());

        String mbid = "mbid";

        when(userArtistService.getArtistsAndSimilar(user, mbid))
                .thenReturn(List.of());

        when(userArtistRelationService.getLinks(user, mbid))
                .thenReturn(List.of());

        GraphResponse response = graphService.getDiscoveryGraph(user, mbid);

        assertTrue(response.nodes().isEmpty());
        assertTrue(response.links().isEmpty());
    }

    @Test
    void should_fail_when_user_artist_service_breaks() {

        User user = new User();
        user.setId(UUID.randomUUID());

        String mbid = "mbid";

        when(userArtistService.getArtistsAndSimilar(user, mbid))
                .thenThrow(new RuntimeException("fail"));

        assertThrows(RuntimeException.class,
                () -> graphService.getDiscoveryGraph(user, mbid));

        verify(userArtistRelationService, never()).getLinks(any(), any());
    }

    @Test
    void should_fail_when_relation_service_breaks() {

        User user = new User();
        user.setId(UUID.randomUUID());

        String mbid = "mbid";

        when(userArtistService.getArtistsAndSimilar(user, mbid))
                .thenReturn(List.of());

        when(userArtistRelationService.getLinks(user, mbid))
                .thenThrow(new RuntimeException("fail"));

        assertThrows(RuntimeException.class,
                () -> graphService.getDiscoveryGraph(user, mbid));
    }
}
