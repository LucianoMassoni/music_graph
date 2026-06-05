package com.luciano.music_graph.service;

import com.luciano.music_graph.client.LastFmClient;
import com.luciano.music_graph.dto.ArtistDetail;
import com.luciano.music_graph.dto.lastfm.*;
import com.luciano.music_graph.mapper.ArtistMapper;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.ArtistSource;
import com.luciano.music_graph.repository.ArtistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @InjectMocks
    private ArtistService artistService;

    @Mock
    private LastFmClient lastFmClient;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ArtistTagService tagService;

    @Mock
    private AlbumService albumService;

    private ArtistMapper mapper = Mappers.getMapper(ArtistMapper.class);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(artistService, "mapper", mapper);
    }



    @Test
    void should_return_artist_without_enrich_when_source_is_api_imported() {

        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());
        artist.setMbid("mbid");
        artist.setSource(ArtistSource.API_IMPORTED);
        artist.setEnriched(true);

        when(artistRepository.findByMbid("mbid")).thenReturn(Optional.of(artist));
        when(tagService.getAllTagDataByArtistId(artist.getId())).thenReturn(List.of());
        when(albumService.getAllAlbumDataByArtistId(artist.getId())).thenReturn(List.of());

        ArtistDetail result = artistService.getOrImport("mbid");

        verify(lastFmClient, never()).getInfo(any());
        verify(artistRepository, never()).save(any());

        assertEquals("mbid", result.mbid());
        assertEquals(artist.getId(), result.id());
    }


    @Test
    void should_import_artist_when_not_found() {

        String mbid = "mbid";

        LFArtistInfo lfInfo = mock(LFArtistInfo.class);
        LFArtistInfoResponse response = mock(LFArtistInfoResponse.class);

        when(response.artist()).thenReturn(lfInfo);
        when(lfInfo.bio()).thenReturn(mock(LFBio.class));

        when(lastFmClient.getInfo(mbid)).thenReturn(response);
        when(lastFmClient.getTopTags(mbid)).thenReturn(mock(LFTopTagsResponse.class));
        when(lastFmClient.getAlbums(mbid)).thenReturn(mock(LFAlbumResponse.class));

        Artist saved = new Artist();
        saved.setId(UUID.randomUUID());
        saved.setMbid(mbid);
        saved.setSource(ArtistSource.API_IMPORTED);
        saved.setEnriched(true);

        when(artistRepository.save(any())).thenReturn(saved);
        when(artistRepository.findByMbid(mbid)).thenReturn(Optional.empty());

        when(tagService.getAllTagDataByArtistId(any())).thenReturn(List.of());
        when(albumService.getAllAlbumDataByArtistId(any())).thenReturn(List.of());

        ArtistDetail result = artistService.getOrImport(mbid);

        verify(lastFmClient, times(1)).getInfo(mbid);
        verify(artistRepository, times(1)).save(any());
        verify(tagService).saveAllTagsInArtist(any(), any());
        verify(albumService).saveAllAlbumInArtist(any(), any());

        assertNotNull(result);
    }

    @Test
    void should_enrich_related_artist_when_not_enriched() {

        String mbid = "mbid";

        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());
        artist.setMbid(mbid);
        artist.setSource(ArtistSource.RELATED);
        artist.setEnriched(false);

        LFArtistInfoResponse response = mock(LFArtistInfoResponse.class);
        LFArtistInfo lfInfo = mock(LFArtistInfo.class);
        LFBio bio = mock(LFBio.class);

        when(response.artist()).thenReturn(lfInfo);
        when(lfInfo.bio()).thenReturn(bio);
        when(bio.content()).thenReturn("bio");

        when(lfInfo.image()).thenReturn(List.of());

        when(lastFmClient.getInfo(mbid)).thenReturn(response);
        when(lastFmClient.getTopTags(mbid)).thenReturn(mock(LFTopTagsResponse.class));
        when(lastFmClient.getAlbums(mbid)).thenReturn(mock(LFAlbumResponse.class));

        when(artistRepository.findByMbid(mbid)).thenReturn(Optional.of(artist));

        when(tagService.getAllTagDataByArtistId(any())).thenReturn(List.of());
        when(albumService.getAllAlbumDataByArtistId(any())).thenReturn(List.of());

        artistService.getOrImport(mbid);

        verify(lastFmClient, times(1)).getInfo(mbid);
        verify(artistRepository).save(artist);

        assertTrue(artist.isEnriched());
        assertEquals("bio", artist.getBio());
    }


    @Test
    void should_not_enrich_if_already_enriched() {

        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());
        artist.setMbid("mbid");
        artist.setSource(ArtistSource.RELATED);
        artist.setEnriched(true);

        when(artistRepository.findByMbid("mbid")).thenReturn(Optional.of(artist));

        when(tagService.getAllTagDataByArtistId(any())).thenReturn(List.of());
        when(albumService.getAllAlbumDataByArtistId(any())).thenReturn(List.of());

        artistService.getOrImport("mbid");

        verify(lastFmClient, never()).getInfo(any());
        verify(artistRepository, never()).save(any());
    }
}