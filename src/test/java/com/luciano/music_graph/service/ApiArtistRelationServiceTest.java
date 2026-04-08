package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.ApiArtistRelationResponse;
import com.luciano.music_graph.dto.lastfm.LFSimilarArtist;
import com.luciano.music_graph.dto.lastfm.LFSimilarArtistInfo;
import com.luciano.music_graph.dto.lastfm.LFSimilarArtistResponse;
import com.luciano.music_graph.mapper.ApiArtistRelationMapper;
import com.luciano.music_graph.mapper.ApiArtistRelationMapperImpl;
import com.luciano.music_graph.model.ApiArtistRelation;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.repository.ApiArtistRelationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
public class ApiArtistRelationServiceTest {

    @InjectMocks
    private ApiArtistRelationService apiArtistRelationService;

    @Mock
    private ApiArtistRelationRepository relationRepository;

    private ApiArtistRelationMapper mapper;

    @Mock
    private ArtistService artistService;

    @BeforeEach
    void setUp(){
        mapper = new ApiArtistRelationMapperImpl();
        ReflectionTestUtils.setField(apiArtistRelationService, "mapper", mapper);
    }

    @Test
    void buildApiRelations_(){

        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());
        artist.setName("artist");
        artist.setMbid("mbid");

        Artist related1 = new Artist();
        related1.setId(UUID.randomUUID());
        related1.setName("related_1");
        related1.setMbid("mbid_r1");

        Artist related2 = new Artist();
        related2.setId(UUID.randomUUID());
        related2.setName("related_2");
        related2.setMbid("mbid_r2");

        LFSimilarArtistResponse similarArtistResponse = new LFSimilarArtistResponse(
                new LFSimilarArtist(new ArrayList<>(List.of(
                        new LFSimilarArtistInfo(related1.getName(), related1.getMbid(), "1"),
                        new LFSimilarArtistInfo(related2.getName(), related2.getMbid(), "0.95")
                    )))
        );

        when(artistService.findByMbid("mbid_r1")).thenReturn(Optional.empty());
        when(artistService.findByMbid("mbid_r2")).thenReturn(Optional.empty());
        when(artistService.saveBasic("related_1", "mbid_r1")).thenReturn(related1);
        when(artistService.saveBasic("related_2", "mbid_r2")).thenReturn(related2);
        when(relationRepository.findByArtists(any(), any())).thenReturn(Optional.empty());
        when(relationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApiArtistRelationResponse response = apiArtistRelationService.buildApiRelations(artist, similarArtistResponse);

        verify(artistService).findByMbid("mbid_r1");
        verify(artistService).findByMbid("mbid_r2");
        verify(artistService).saveBasic("related_1", "mbid_r1");
        verify(artistService).saveBasic("related_2", "mbid_r2");

        assertEquals(response.artist().id(), artist.getId());
        assertEquals(response.artist().name(), artist.getName());
        assertEquals(response.artist().mbid(), artist.getMbid());
        assertEquals(2, response.relatedArtists().size());
        assertEquals(100, response.relatedArtists().getFirst().weight());
        assertEquals(related1.getId(), response.relatedArtists().getFirst().artist().id());
        assertEquals(related1.getName(), response.relatedArtists().getFirst().artist().name());
        assertEquals(related1.getMbid(), response.relatedArtists().getFirst().artist().mbid());
        assertEquals(95, response.relatedArtists().getLast().weight());
        assertEquals(related2.getId(), response.relatedArtists().getLast().artist().id());
        assertEquals(related2.getName(), response.relatedArtists().getLast().artist().name());
        assertEquals(related2.getMbid(), response.relatedArtists().getLast().artist().mbid());
    }

    @Test
    void buildApiRelations_shouldIgnoreArtistsWithoutMbid() {

        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());

        LFSimilarArtistResponse response = new LFSimilarArtistResponse(
                new LFSimilarArtist(List.of(
                        new LFSimilarArtistInfo("related", null, "0.5")
                ))
        );

        ApiArtistRelationResponse result = apiArtistRelationService.buildApiRelations(artist, response);

        assertTrue(result.relatedArtists().isEmpty());

        verify(artistService, never()).findByMbid(any());
        verify(artistService, never()).saveBasic(any(), any());
    }

    @Test
    void buildApiRelations_shouldNotUpdateWeight_ifExistingIsGreater() {

        Artist artist = new Artist();
        artist.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        Artist related = new Artist();
        related.setId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        related.setMbid("mbid_r1");

        ApiArtistRelation existing = new ApiArtistRelation();
        existing.setId(UUID.randomUUID());
        existing.setArtistA(artist);
        existing.setArtistB(related);
        existing.setWeight(100); // mayor

        LFSimilarArtistResponse response = new LFSimilarArtistResponse(
                new LFSimilarArtist(List.of(
                        new LFSimilarArtistInfo("related", "mbid_r1", "0.5") // 50
                ))
        );

        when(artistService.findByMbid("mbid_r1")).thenReturn(Optional.of(related));
        when(relationRepository.findByArtists(
                argThat(a -> a.getId().equals(artist.getId())),
                argThat(b -> b.getId().equals(related.getId()))
        )).thenReturn(Optional.of(existing));

        ApiArtistRelationResponse result = apiArtistRelationService.buildApiRelations(artist, response);

        verify(relationRepository, never()).save(existing);
    }

    @Test
    void buildApiRelations_shouldUpdateWeight_ifNewIsGreater() {

        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());

        Artist related = new Artist();
        related.setId(UUID.randomUUID());
        related.setMbid("mbid_r1");

        ApiArtistRelation existing = new ApiArtistRelation();
        existing.setArtistA(artist);
        existing.setArtistB(related);
        existing.setWeight(50);

        LFSimilarArtistResponse response = new LFSimilarArtistResponse(
                new LFSimilarArtist(List.of(
                        new LFSimilarArtistInfo("related", "mbid_r1", "0.8")
                ))
        );

        when(artistService.findByMbid("mbid_r1")).thenReturn(Optional.of(related));
        when(relationRepository.findByArtists(any(), any())).thenReturn(Optional.of(existing));
        when(relationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        apiArtistRelationService.buildApiRelations(artist, response);

        assertEquals(80, existing.getWeight());
        verify(relationRepository).save(existing);
    }


    @Test
    void buildApiRelations_shouldOrderArtistsCorrectly() {

        Artist artist = new Artist();
        artist.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        Artist related = new Artist();
        related.setId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        related.setMbid("mbid_r1");

        LFSimilarArtistResponse response = new LFSimilarArtistResponse(
                new LFSimilarArtist(List.of(
                        new LFSimilarArtistInfo("related", "mbid_r1", "0.5")
                ))
        );

        when(artistService.findByMbid("mbid_r1")).thenReturn(Optional.of(related));
        when(relationRepository.findByArtists(any(), any())).thenReturn(Optional.empty());
        when(relationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        apiArtistRelationService.buildApiRelations(artist, response);

        ArgumentCaptor<ApiArtistRelation> captor = ArgumentCaptor.forClass(ApiArtistRelation.class);

        verify(relationRepository).save(captor.capture());

        ApiArtistRelation saved = captor.getValue();

        assertEquals(artist, saved.getArtistA());
        assertEquals(related, saved.getArtistB());
    }

}
