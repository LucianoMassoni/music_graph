package com.luciano.music_graph.service;

import com.luciano.music_graph.client.LastFmClient;
import com.luciano.music_graph.dto.AlbumDetail;
import com.luciano.music_graph.dto.ArtistDetail;
import com.luciano.music_graph.dto.ArtistSearchResult;
import com.luciano.music_graph.dto.ArtistTagData;
import com.luciano.music_graph.dto.lastfm.*;
import com.luciano.music_graph.mapper.ArtistMapper;
import com.luciano.music_graph.mapper.ArtistMapperImpl;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.repository.ArtistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArtistServiceTest {

    @InjectMocks
    private ArtistService artistService;

    @Mock
    private LastFmClient lastFmClient;

    @Mock
    private ArtistRepository artistRepository;

    private ArtistMapper mapper = new ArtistMapperImpl();

    @Mock
    private ArtistTagService tagService;

    @Mock
    private AlbumService albumService;


    @BeforeEach
    void setUp() {
        mapper = new ArtistMapperImpl();
        ReflectionTestUtils.setField(artistService, "mapper", mapper);
    }

    @Test
    void search_shouldFiltrateAndReturnOne(){

        List<LFArtist> artists = List.of(
                new LFArtist("artist1", "fsdfk"),
                new LFArtist("artist2", "")
        );

        when(lastFmClient.search(any())).thenReturn(new LFSearchResponse(
                new LFArtistMatches(
                        new LFArtistSearchResult(artists))
        ));


        ArtistSearchResult result = artistService.search("artist");

        verify(lastFmClient).search("artist");

        assertEquals(1, result.artist().size());
        assertEquals(result.artist().getFirst().name(), artists.getFirst().name());
        assertEquals(result.artist().getFirst().mbid(), artists.getFirst().mbid());
    }

    @Test
    void search_shouldDeleteMbidDuplicate(){

        List<LFArtist> artists = List.of(
                new LFArtist("artist1", "fsdfk"),
                new LFArtist("artist2", "fsdfk")
        );

        when(lastFmClient.search(any())).thenReturn(new LFSearchResponse(
                new LFArtistMatches(
                        new LFArtistSearchResult(artists))
        ));

        ArtistSearchResult result = artistService.search("artist");

        verify(lastFmClient).search("artist");

        assertEquals(1, result.artist().size());
        assertEquals(result.artist().getFirst().name(), artists.getFirst().name());
        assertEquals(result.artist().getFirst().mbid(), artists.getFirst().mbid());
    }

    @Test
    void getOrImport_shouldGet(){

        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());
        artist.setMbid("mbid");
        artist.setName("artist");

        AlbumDetail albumDetail = new AlbumDetail("album", 2000, "img_url", "lastFmUrl");
        ArtistTagData artistTagData = new ArtistTagData("tag_name", 100);

        when(artistRepository.findByMbid(any())).thenReturn(Optional.of(artist));
        when(tagService.getAllTagDataByArtistId(any())).thenReturn(List.of(artistTagData));
        when(albumService.getAllAlbumDataByArtistId(any())).thenReturn(List.of(albumDetail));

        ArtistDetail artistDetail = artistService.getOrImport("mbid");

        verify(artistRepository).findByMbid("mbid");
        verify(tagService).getAllTagDataByArtistId(artist.getId());
        verify(albumService).getAllAlbumDataByArtistId(artist.getId());

        assertEquals(artistDetail.id(), artist.getId());
        assertEquals(artistDetail.mbid(), artist.getMbid());
        assertEquals(artistDetail.name(), artist.getName());
        assertTrue(artistDetail.tags().contains(artistTagData));
        assertTrue(artistDetail.albums().contains(albumDetail));
    }

    @Test
    void getOrImport_shouldImport(){

        String mbid = "mbid";

        // repo no encuentra nada
        when(artistRepository.findByMbid(mbid)).thenReturn(Optional.empty());

        // mock de respuestas de LastFm
        LFArtistInfoResponse infoResponse = mock(LFArtistInfoResponse.class);
        LFTopTagsResponse tagsResponse = mock(LFTopTagsResponse.class);
        LFAlbumResponse albumResponse = mock(LFAlbumResponse.class);

        when(lastFmClient.getInfo(mbid)).thenReturn(infoResponse);
        when(lastFmClient.getTopTags(mbid)).thenReturn(tagsResponse);
        when(lastFmClient.getAlbums(mbid)).thenReturn(albumResponse);

        // mapper → entity
        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());
        artist.setMbid(mbid);
        artist.setName("artist");


        // save devuelve el mismo objeto
        when(artistRepository.save(any())).thenReturn(artist);

        // mocks de servicios
        when(tagService.getAllTagDataByArtistId(any())).thenReturn(List.of());
        when(albumService.getAllAlbumDataByArtistId(any())).thenReturn(List.of());

        // ejecutar
        ArtistDetail result = artistService.getOrImport(mbid);

        // verify IMPORT
        verify(lastFmClient).getInfo(mbid);
        verify(lastFmClient).getTopTags(mbid);
        verify(lastFmClient).getAlbums(mbid);

        verify(artistRepository).save(any());

        verify(tagService).saveAllTagsInArtist(any(), eq(artist));
        verify(albumService).saveAllAlbumInArtist(any(), eq(artist));

        verify(tagService).getAllTagDataByArtistId(artist.getId());
        verify(albumService).getAllAlbumDataByArtistId(artist.getId());

        assertNotNull(result);
    }

    @Test
    void getOrImport_shouldBeIdempotent() {

        String mbid = "mbid";

        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());
        artist.setMbid(mbid);
        artist.setName("artist");

        // 1ra vez no existe, 2da sí
        when(artistRepository.findByMbid(mbid))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(artist));

        // mocks LastFm
        LFArtistInfoResponse infoResponse = mock(LFArtistInfoResponse.class);
        LFTopTagsResponse tagsResponse = mock(LFTopTagsResponse.class);
        LFAlbumResponse albumResponse = mock(LFAlbumResponse.class);

        when(lastFmClient.getInfo(mbid)).thenReturn(infoResponse);
        when(lastFmClient.getTopTags(mbid)).thenReturn(tagsResponse);
        when(lastFmClient.getAlbums(mbid)).thenReturn(albumResponse);

        when(artistRepository.save(any())).thenReturn(artist);

        when(tagService.getAllTagDataByArtistId(any())).thenReturn(List.of());
        when(albumService.getAllAlbumDataByArtistId(any())).thenReturn(List.of());

        // 1ra llamada → importa
        artistService.getOrImport(mbid);

        // 2da llamada → debería usar DB
        artistService.getOrImport(mbid);

        // verify: LastFm SOLO UNA VEZ
        verify(lastFmClient, times(1)).getInfo(mbid);
        verify(lastFmClient, times(1)).getTopTags(mbid);
        verify(lastFmClient, times(1)).getAlbums(mbid);

        // save SOLO UNA VEZ
        verify(artistRepository, times(1)).save(any());

        // find se llama 2 veces
        verify(artistRepository, times(2)).findByMbid(mbid);
    }

    @Test
    void getOrImport_shouldThrowWhenGetInfoFails() {

        String mbid = "mbid";

        when(artistRepository.findByMbid(mbid))
                .thenReturn(Optional.empty());

        when(lastFmClient.getInfo(mbid))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> {
            artistService.getOrImport(mbid);
        });

        verify(artistRepository, never()).save(any());
        verify(tagService, never()).saveAllTagsInArtist(any(), any());
        verify(albumService, never()).saveAllAlbumInArtist(any(), any());
    }

    @Test
    void getOrImport_shouldThrowWhenGetTopTagsFails() {

        String mbid = "mbid";

        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());

        when(artistRepository.findByMbid(mbid))
                .thenReturn(Optional.empty());

        when(lastFmClient.getInfo(mbid)).thenReturn(mock(LFArtistInfoResponse.class));
        when(artistRepository.save(any())).thenReturn(artist);

        when(lastFmClient.getTopTags(mbid))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> {
            artistService.getOrImport(mbid);
        });

        verify(artistRepository).save(any());
        verify(albumService, never()).saveAllAlbumInArtist(any(), any());
    }

    @Test
    void getOrImport_shouldThrowWhenGetAlbumsFails() {

        String mbid = "mbid";

        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());

        when(artistRepository.findByMbid(mbid))
                .thenReturn(Optional.empty());

        when(lastFmClient.getInfo(mbid)).thenReturn(mock(LFArtistInfoResponse.class));
        when(artistRepository.save(any())).thenReturn(artist);

        when(lastFmClient.getTopTags(mbid)).thenReturn(mock(LFTopTagsResponse.class));

        when(lastFmClient.getAlbums(mbid))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> {
            artistService.getOrImport(mbid);
        });

        verify(artistRepository).save(any());
        verify(tagService).saveAllTagsInArtist(any(), eq(artist));
    }
}
