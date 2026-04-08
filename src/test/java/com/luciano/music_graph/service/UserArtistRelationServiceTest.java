package com.luciano.music_graph.service;

import com.luciano.music_graph.mapper.UserArtistRelationMapper;
import com.luciano.music_graph.mapper.UserArtistRelationMapperImpl;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.model.UserArtistRelation;
import com.luciano.music_graph.repository.UserArtistRelationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserArtistRelationServiceTest {

    @InjectMocks
    private UserArtistRelationService userArtistRelationService;

    @Mock
    private UserArtistRelationRepository relationRepository;

    private UserArtistRelationMapper mapper;

    private final Integer TAG_WEIGHT = 10;

    @BeforeEach
    void setUp(){
        mapper = new UserArtistRelationMapperImpl();
        ReflectionTestUtils.setField(userArtistRelationService, "mapper", mapper);
        ReflectionTestUtils.setField(userArtistRelationService, "TAG_WEIGHT", TAG_WEIGHT);
    }

    @Test
    void recalculateFromTags_shouldCreateRelation_whenNotExists() {

        User user = new User();
        Artist artist = new Artist();
        artist.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        Artist related = new Artist();
        related.setId(UUID.fromString("00000000-0000-0000-0000-000000000002"));

        List<Object[]> results = Collections.singletonList(
                new Object[]{related, 2L}
        );

        when(relationRepository.getUserArtistTagByUserAndArtist(user, artist)).thenReturn(results);
        when(relationRepository.getEntityByUserAndArtist(user, artist, related)).thenReturn(Optional.empty());
        when(relationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userArtistRelationService.recalculateFromTags(user, artist);

        verify(relationRepository).save(argThat(rel ->
                rel.getWeight().equals(2 * TAG_WEIGHT)
        ));
    }

    @Test
    void recalculateFromTags_shouldDeleteRelation_whenWeightIsZero() {

        User user = new User();
        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());

        Artist related = new Artist();
        related.setId(UUID.randomUUID());

        List<Object[]> results = Collections.singletonList(
                new Object[]{related, 0L}
        );

        UserArtistRelation relation = new UserArtistRelation();

        when(relationRepository.getUserArtistTagByUserAndArtist(user, artist)).thenReturn(results);
        when(relationRepository.getEntityByUserAndArtist(any(), any(), any())).thenReturn(Optional.of(relation));

        userArtistRelationService.recalculateFromTags(user, artist);

        verify(relationRepository).delete(relation);
        verify(relationRepository, never()).save(any());
    }


    @Test
    void recalculateFromTags_shouldUpdateExistingRelation() {

        User user = new User();
        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());

        Artist related = new Artist();
        related.setId(UUID.randomUUID());

        List<Object[]> results = Collections.singletonList(
                new Object[]{related, 3L}
        );

        UserArtistRelation existing = new UserArtistRelation();

        when(relationRepository.getUserArtistTagByUserAndArtist(user, artist)).thenReturn(results);
        when(relationRepository.getEntityByUserAndArtist(any(), any(), any())).thenReturn(Optional.of(existing));
        when(relationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userArtistRelationService.recalculateFromTags(user, artist);

        assertEquals(3 * TAG_WEIGHT, existing.getWeight());
        verify(relationRepository).save(existing);
    }


    @Test
    void recalculateFromTags_shouldContinueProcessingAfterZeroWeight() {

        User user = new User();
        Artist artist = new Artist();
        artist.setId(UUID.randomUUID());

        Artist related1 = new Artist();
        related1.setId(UUID.randomUUID());

        Artist related2 = new Artist();
        related2.setId(UUID.randomUUID());

        List<Object[]> results = List.of(
                new Object[]{related1, 0L}, // se elimina
                new Object[]{related2, 5L}  // se guarda
        );

        when(relationRepository.getUserArtistTagByUserAndArtist(user, artist)).thenReturn(results);
        when(relationRepository.getEntityByUserAndArtist(any(), any(), any())).thenReturn(Optional.of(new UserArtistRelation()));
        when(relationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userArtistRelationService.recalculateFromTags(user, artist);

        verify(relationRepository, times(1)).delete(any());
        verify(relationRepository, times(1)).save(any());

        verify(relationRepository).delete(any());
        verify(relationRepository).save(argThat(rel ->
                rel.getWeight().equals(5 * TAG_WEIGHT)
        ));
    }

    @Test
    void recalculateFromTags_shouldOrderArtistsCorrectly() {

        User user = new User();

        Artist artist = new Artist();
        artist.setId(UUID.fromString("00000000-0000-0000-0000-000000000002")); // mayor

        Artist related = new Artist();
        related.setId(UUID.fromString("00000000-0000-0000-0000-000000000001")); // menor

        List<Object[]> results = Collections.singletonList(
                new Object[]{related, 2L}
        );

        when(relationRepository.getUserArtistTagByUserAndArtist(user, artist)).thenReturn(results);
        when(relationRepository.getEntityByUserAndArtist(any(), any(), any())).thenReturn(Optional.empty());
        when(relationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userArtistRelationService.recalculateFromTags(user, artist);

        ArgumentCaptor<UserArtistRelation> captor = ArgumentCaptor.forClass(UserArtistRelation.class);

        verify(relationRepository).save(captor.capture());

        UserArtistRelation saved = captor.getValue();

        assertTrue(saved.getArtistA().getId().toString()
                .compareTo(saved.getArtistB().getId().toString()) < 0);
    }

}
