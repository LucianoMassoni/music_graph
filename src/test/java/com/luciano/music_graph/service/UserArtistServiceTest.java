package com.luciano.music_graph.service;

import com.luciano.music_graph.client.LastFmClient;
import com.luciano.music_graph.dto.ArtistSearchData;
import com.luciano.music_graph.dto.ArtistSearchResult;
import com.luciano.music_graph.dto.graph.Node;
import com.luciano.music_graph.dto.lastfm.*;
import com.luciano.music_graph.mapper.UserArtistMapper;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.model.UserArtist;
import com.luciano.music_graph.repository.UserArtistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserArtistServiceTest {

    @InjectMocks
    private UserArtistService userArtistService;

    @Mock
    private UserArtistRepository userArtistRepository;

    @Mock
    private ArtistService artistService;

    @Mock
    private ApiArtistRelationService apiArtistRelationService;

    @Mock
    private LastFmClient lastFmClient;

    @Mock
    private UserArtistMapper mapper;


    @Test
    void should_follow_artist_when_not_existing_relation() {

        User user = new User();
        Artist artist = new Artist();
        artist.setMbid("mbid");

        when(artistService.findByMbid("mbid")).thenReturn(Optional.of(artist));
        when(userArtistRepository.findByUserAndArtist(user, artist)).thenReturn(Optional.empty());

        UserArtist saved = new UserArtist();
        saved.setFollowed(true);

        when(userArtistRepository.save(any())).thenReturn(saved);

        userArtistService.followArtist(user, "mbid");

        verify(userArtistRepository).save(any(UserArtist.class));
    }

    @Test
    void should_not_duplicate_follow_if_already_followed() {

        User user = new User();
        Artist artist = new Artist();
        artist.setMbid("mbid");

        UserArtist existing = new UserArtist();
        existing.setFollowed(true);

        when(artistService.findByMbid("mbid")).thenReturn(Optional.of(artist));
        when(userArtistRepository.findByUserAndArtist(user, artist))
                .thenReturn(Optional.of(existing));

        userArtistService.followArtist(user, "mbid");

        verify(userArtistRepository, never()).save(any());
    }


    @Test
    void should_unfollow_artist_when_followed() {

        User user = new User();
        Artist artist = new Artist();
        artist.setMbid("mbid");

        UserArtist ua = new UserArtist();
        ua.setFollowed(true);

        when(artistService.findByMbid("mbid")).thenReturn(Optional.of(artist));
        when(userArtistRepository.findByUserAndArtist(user, artist)).thenReturn(Optional.of(ua));

        userArtistService.unfollowArtist(user, "mbid");

        verify(userArtistRepository).save(ua);
        assertFalse(ua.isFollowed());
    }

    @Test
    void should_not_unfollow_if_already_unfollowed() {

        User user = new User();
        Artist artist = new Artist();
        artist.setMbid("mbid");

        UserArtist ua = new UserArtist();
        ua.setFollowed(false);

        when(artistService.findByMbid("mbid")).thenReturn(Optional.of(artist));
        when(userArtistRepository.findByUserAndArtist(user, artist)).thenReturn(Optional.of(ua));

        userArtistService.unfollowArtist(user, "mbid");

        verify(userArtistRepository, never()).save(any());
    }


    @Test
    void should_search_and_map_follow_state() {

        User user = new User();

        LFSearchResponse response = mock(LFSearchResponse.class);
        LFArtistMatches results = mock(LFArtistMatches.class);
        LFArtistSearchResult matches = mock(LFArtistSearchResult.class);

        LFArtist artist = mock(LFArtist.class);

        when(lastFmClient.search("metal")).thenReturn(response);
        when(response.results()).thenReturn(results);
        when(results.artistmatches()).thenReturn(matches);
        when(matches.artist()).thenReturn(List.of(artist));

        when(artist.mbid()).thenReturn("mbid");

        when(userArtistRepository.isFollowed(user, "mbid")).thenReturn(Optional.of(true));

        when(mapper.toArtistSearchData(any(), eq(true)))
                .thenReturn(mock(ArtistSearchData.class));

        ArtistSearchResult result = userArtistService.search(user, "metal");

        verify(lastFmClient).search("metal");
        assertEquals(1, result.nodes().size());
    }


    @Test
    void should_build_graph_and_call_relation_service() {

        User user = new User();

        Artist artist = new Artist();
        artist.setMbid("mbid");
        artist.setName("artist");

        when(artistService.findByMbid("mbid")).thenReturn(Optional.of(artist));

        LFSimilarArtistResponse similar = mock(LFSimilarArtistResponse.class);

        when(lastFmClient.getSimilar("mbid")).thenReturn(similar);

        UserArtist ua = new UserArtist();
        ua.setFollowed(true);

        when(userArtistRepository.findByUserAndArtist(user, artist))
                .thenReturn(Optional.of(ua));

        when(userArtistRepository.getAllNodesByUserAndArtist(user, artist))
                .thenReturn(List.of(
                        new Object[]{"A", "mbidA", true},
                        new Object[]{"B", "mbidB", false}
                ));

        List<Node> result = userArtistService.getArtistsAndSimilar(user, "mbid");

        verify(apiArtistRelationService).buildApiRelations(artist, similar);
        verify(lastFmClient).getSimilar("mbid");

        assertEquals(3, result.size()); // root + 2 nodes
    }


    @Test
    void should_return_followed_nodes() {

        User user = new User();

        Artist artist = new Artist();
        artist.setName("A");
        artist.setMbid("mbidA");

        UserArtist ua = new UserArtist();
        ua.setArtist(artist);
        ua.setFollowed(true);

        when(userArtistRepository.getAllFollowedByUser(user))
                .thenReturn(List.of(ua));

        List<Node> nodes = userArtistService.getFollowedNodes(user);

        assertEquals(1, nodes.size());
        assertEquals("mbidA", nodes.getFirst().id());
    }
}
