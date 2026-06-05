package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.lastfm.LFSimilarArtist;
import com.luciano.music_graph.dto.lastfm.LFSimilarArtistInfo;
import com.luciano.music_graph.dto.lastfm.LFSimilarArtistResponse;
import com.luciano.music_graph.mapper.ApiArtistRelationMapper;
import com.luciano.music_graph.model.ApiArtistRelation;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.repository.ApiArtistRelationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
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
class ApiArtistRelationServiceTest {

    @InjectMocks
    private ApiArtistRelationService service;

    @Mock
    private ApiArtistRelationRepository relationRepository;

    @Mock
    private ArtistService artistService;

    private ApiArtistRelationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ApiArtistRelationMapper.class);
        ReflectionTestUtils.setField(service, "mapper", mapper);
    }

    @Test
    void should_ignore_artists_without_mbid() {

        Artist artist = artist("artist", "mbid_artist");

        LFSimilarArtistResponse response =
                response(
                        new LFSimilarArtistInfo("related", null, "0.5")
                );

        service.buildApiRelations(artist, response);

        verifyNoInteractions(artistService);
        verifyNoInteractions(relationRepository);
    }

    @Test
    void should_create_artist_when_not_exists() {

        Artist artist = artist("artist", "mbid_artist");

        Artist related = artist("related", "mbid_related");

        when(artistService.findByMbid("mbid_related"))
                .thenReturn(Optional.empty());

        when(artistService.saveBasic("related", "mbid_related"))
                .thenReturn(related);

        when(relationRepository.findByArtists(any(), any()))
                .thenReturn(Optional.empty());

        service.buildApiRelations(
                artist,
                response(new LFSimilarArtistInfo("related", "mbid_related", "0.5"))
        );

        verify(artistService)
                .saveBasic("related", "mbid_related");
    }

    @Test
    void should_not_create_artist_when_artist_exists() {

        Artist artist = artist("artist", "mbid_artist");
        Artist related = artist("related", "mbid_related");

        when(artistService.findByMbid("mbid_related"))
                .thenReturn(Optional.of(related));

        when(relationRepository.findByArtists(any(), any()))
                .thenReturn(Optional.empty());

        service.buildApiRelations(
                artist,
                response(new LFSimilarArtistInfo("related", "mbid_related", "0.5"))
        );

        verify(artistService, never())
                .saveBasic(any(), any());
    }

    @Test
    void should_create_relation_when_not_exists() {

        Artist artist = artist("artist", "aaa");
        Artist related = artist("related", "bbb");

        when(artistService.findByMbid("bbb"))
                .thenReturn(Optional.of(related));

        when(relationRepository.findByArtists(any(), any()))
                .thenReturn(Optional.empty());

        ArgumentCaptor<ApiArtistRelation> captor =
                ArgumentCaptor.forClass(ApiArtistRelation.class);

        service.buildApiRelations(
                artist,
                response(new LFSimilarArtistInfo("related", "bbb", "0.5"))
        );

        verify(relationRepository).save(captor.capture());

        ApiArtistRelation saved = captor.getValue();

        assertEquals(50, saved.getWeight());
        assertEquals(artist, saved.getArtistA());
        assertEquals(related, saved.getArtistB());
    }

    @Test
    void should_update_weight_when_new_weight_is_higher() {

        Artist artist = artist("artist", "aaa");
        Artist related = artist("related", "bbb");

        ApiArtistRelation relation = new ApiArtistRelation();
        relation.setWeight(30);

        when(artistService.findByMbid("bbb"))
                .thenReturn(Optional.of(related));

        when(relationRepository.findByArtists(any(), any()))
                .thenReturn(Optional.of(relation));

        service.buildApiRelations(
                artist,
                response(new LFSimilarArtistInfo("related", "bbb", "0.9"))
        );

        assertEquals(90, relation.getWeight());

        verify(relationRepository)
                .save(relation);
    }

    @Test
    void should_not_update_weight_when_existing_weight_is_higher() {

        Artist artist = artist("artist", "aaa");
        Artist related = artist("related", "bbb");

        ApiArtistRelation relation = new ApiArtistRelation();
        relation.setWeight(90);

        when(artistService.findByMbid("bbb"))
                .thenReturn(Optional.of(related));

        when(relationRepository.findByArtists(any(), any()))
                .thenReturn(Optional.of(relation));

        service.buildApiRelations(
                artist,
                response(new LFSimilarArtistInfo("related", "bbb", "0.5"))
        );

        assertEquals(90, relation.getWeight());

        verify(relationRepository, never())
                .save(relation);
    }

    @Test
    void should_order_artists_by_mbid() {

        Artist artist = artist("artist", "zzz");
        Artist related = artist("related", "aaa");

        when(artistService.findByMbid("aaa"))
                .thenReturn(Optional.of(related));

        when(relationRepository.findByArtists(any(), any()))
                .thenReturn(Optional.empty());

        ArgumentCaptor<ApiArtistRelation> captor =
                ArgumentCaptor.forClass(ApiArtistRelation.class);

        service.buildApiRelations(
                artist,
                response(new LFSimilarArtistInfo("related", "aaa", "0.5"))
        );

        verify(relationRepository)
                .save(captor.capture());

        ApiArtistRelation saved = captor.getValue();

        assertEquals("aaa", saved.getArtistA().getMbid());
        assertEquals("zzz", saved.getArtistB().getMbid());
    }

    @Test
    void should_process_multiple_related_artists() {

        Artist artist = artist("artist", "root");

        Artist related1 = artist("related1", "mbid_1");
        Artist related2 = artist("related2", "mbid_2");
        Artist related3 = artist("related3", "mbid_3");

        when(artistService.findByMbid("mbid_1"))
                .thenReturn(Optional.of(related1));

        when(artistService.findByMbid("mbid_2"))
                .thenReturn(Optional.of(related2));

        when(artistService.findByMbid("mbid_3"))
                .thenReturn(Optional.of(related3));

        when(relationRepository.findByArtists(any(), any()))
                .thenReturn(Optional.empty());

        service.buildApiRelations(
                artist,
                response(
                        new LFSimilarArtistInfo("related1", "mbid_1", "0.5"),
                        new LFSimilarArtistInfo("related2", "mbid_2", "0.7"),
                        new LFSimilarArtistInfo("related3", "mbid_3", "0.9")
                )
        );

        verify(relationRepository, times(3))
                .save(any(ApiArtistRelation.class));
    }

    @Test
    void should_handle_empty_response() {

        Artist artist = artist("artist", "mbid_artist");

        LFSimilarArtistResponse response =
                new LFSimilarArtistResponse(
                        new LFSimilarArtist(List.of())
                );

        service.buildApiRelations(artist, response);

        verifyNoInteractions(relationRepository);
    }


    // helpers

    private Artist artist(String name, String mbid) {
        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());
        artist.setName(name);
        artist.setMbid(mbid);
        return artist;
    }

    private LFSimilarArtistResponse response(
            LFSimilarArtistInfo... infos
    ) {
        return new LFSimilarArtistResponse(
                new LFSimilarArtist(List.of(infos))
        );
    }
}
