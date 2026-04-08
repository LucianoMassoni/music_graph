package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.*;
import com.luciano.music_graph.exception.ArtistNotFoundException;
import com.luciano.music_graph.mapper.GraphMapper;
import com.luciano.music_graph.mapper.GraphMapperImpl;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.model.UserArtist;
import com.luciano.music_graph.repository.GraphRepository;
import io.swagger.v3.core.util.ReferenceTypeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GraphServiceTest {

    @InjectMocks
    private GraphService graphService;

    @Mock
    private UserArtistService userArtistService;

    @Mock
    private GraphRepository graphRepository;

    @Mock
    private ArtistService artistService;

    private GraphMapper mapper;

    @BeforeEach
    void setUp(){
        mapper = new GraphMapperImpl();
        ReflectionTestUtils.setField(graphService, "mapper", mapper);
    }


    @Test
    void getLibraryGraph_shouldReturnGraphCorrectly() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Artist artist1 = new Artist();
        artist1.setId(UUID.randomUUID());
        artist1.setName("artist_1");
        artist1.setMbid("a1");

        UserArtist ua = new UserArtist();
        ua.setArtist(artist1);

        List<UserArtist> userArtists = List.of(ua);

        List<Object[]> relations = Collections.singletonList(
                new Object[]{"a1", "a2", 5}
        );

        when(userArtistService.getAllFollowedEntity(user)).thenReturn(userArtists);
        when(graphRepository.getCombinedRelationsForLibrary(user.getId())).thenReturn(relations);

        ArtistNode artistNode = new ArtistNode(List.of(
                new ShortArtistInfoDto(artist1.getId(), artist1.getName(), artist1.getMbid())
        ));
        when(userArtistService.toArtistNode(userArtists)).thenReturn(artistNode);

        LibraryGraphResponse response = graphService.getLibraryGraph(user);

        assertNotNull(response);
        assertEquals(artistNode, response.artistNode());
        assertEquals(1, response.relationEdge().edges().size());

        ArtistRelationship rel = response.relationEdge().edges().getFirst();
        assertEquals("a1", rel.artistAMbid());
        assertEquals("a2", rel.artistBMbid());
        assertEquals(5, rel.weight());
    }

    @Test
    void getLibraryGraph_shouldHandleEmptyRelations() {
        User user = new User();
        user.setId(UUID.randomUUID());

        when(userArtistService.getAllFollowedEntity(user)).thenReturn(List.of());
        when(graphRepository.getCombinedRelationsForLibrary(user.getId())).thenReturn(List.of());
        when(userArtistService.toArtistNode(List.of())).thenReturn(new ArtistNode(List.of()));

        LibraryGraphResponse response = graphService.getLibraryGraph(user);

        assertNotNull(response);
        assertTrue(response.relationEdge().edges().isEmpty());
    }

    @Test
    void getDiscoveryGraph_shouldMapRelatedArtist_whenArtistIsSource() {

        User user = new User();
        user.setId(UUID.randomUUID());

        String mbid = "main";

        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());
        artist.setMbid(mbid);
        artist.setName("Main Artist");

        Artist followed = new Artist();
        followed.setMbid("b1");

        UserArtist ua = new UserArtist();
        ua.setArtist(followed);

        List<UserArtist> userArtists = List.of(ua);

        List<Object[]> relations = Collections.singletonList(
                new Object[]{"main", "Main Artist", "b1", "Artist B", 10}
        );

        when(artistService.findByMbid(mbid)).thenReturn(Optional.of(artist));
        when(userArtistService.getAllFollowedEntity(user)).thenReturn(userArtists);
        when(graphRepository.getCombinedRelationsForDiscovery(eq(user.getId()), eq(mbid), any()))
                .thenReturn(relations);

        DiscoveryGraphResponse response = graphService.getDiscoveryGraph(user, mbid, 10);

        assertEquals(1, response.relatedArtistNodes().size());

        RelatedArtistNode node = response.relatedArtistNodes().getFirst();

        assertEquals("Artist B", node.name());
        assertEquals("b1", node.mbid());
        assertEquals(10, node.weight());
        assertTrue(node.followed());
    }


    @Test
    void getDiscoveryGraph_shouldMapRelatedArtist_whenArtistIsTarget() {

        User user = new User();
        user.setId(UUID.randomUUID());

        String mbid = "main";

        Artist artist = new Artist();
        artist.setMbid(mbid);

        List<UserArtist> userArtists = List.of();

        List<Object[]> relations = Collections.singletonList(
                new Object[]{"a1", "Artist A", "main", "Main Artist", 7}
        );

        when(artistService.findByMbid(mbid)).thenReturn(Optional.of(artist));
        when(userArtistService.getAllFollowedEntity(user)).thenReturn(userArtists);
        when(graphRepository.getCombinedRelationsForDiscovery(eq(user.getId()), eq(mbid), any()))
                .thenReturn(relations);

        DiscoveryGraphResponse response = graphService.getDiscoveryGraph(user, mbid, 10);

        RelatedArtistNode node = response.relatedArtistNodes().getFirst();

        assertEquals("Artist A", node.name());
        assertEquals("a1", node.mbid());
        assertEquals(7, node.weight());
        assertFalse(node.followed());
    }


    @Test
    void getDiscoveryGraph_shouldThrow_whenArtistNotFound() {

        User user = new User();

        when(artistService.findByMbid("invalid")).thenReturn(Optional.empty());

        assertThrows(ArtistNotFoundException.class, () ->
                graphService.getDiscoveryGraph(user, "invalid", 10)
        );
    }

    @Test
    void getDiscoveryGraph_shouldHandleZeroWeight() {

        User user = new User();
        user.setId(UUID.randomUUID());

        String mbid = "main";

        Artist artist = new Artist();
        artist.setMbid(mbid);

        List<Object[]> relations = Collections.singletonList(
                new Object[]{"main", "Main", "b1", "B", 0}
        );

        when(artistService.findByMbid(mbid)).thenReturn(Optional.of(artist));
        when(userArtistService.getAllFollowedEntity(user)).thenReturn(List.of());
        when(graphRepository.getCombinedRelationsForDiscovery(eq(user.getId()), eq(mbid), any()))
                .thenReturn(relations);

        DiscoveryGraphResponse response = graphService.getDiscoveryGraph(user, mbid, 10);

        assertEquals(0, response.relatedArtistNodes().getFirst().weight());
    }

}
