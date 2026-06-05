package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.ArtistRelatedDto;
import com.luciano.music_graph.dto.lastfm.LFSimilarArtistResponse;
import com.luciano.music_graph.mapper.ApiArtistRelationMapper;
import com.luciano.music_graph.model.ApiArtistRelation;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.repository.ApiArtistRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiArtistRelationService {

    private final ApiArtistRelationRepository relationRepository;
    private final ApiArtistRelationMapper mapper;
    private final ArtistService artistService;

    public void buildApiRelations(Artist artist, LFSimilarArtistResponse similarArtistResponse){

        Artist artistA;
        Artist artistB;
        Optional<ApiArtistRelation> relation;

        List<ArtistRelatedDto> artistRelatedDtos = mapper.toArtistRelatedList(similarArtistResponse);

        for (ArtistRelatedDto related : artistRelatedDtos){
            if (related.mbid() == null) continue;

            Optional<Artist> optionalArtist = artistService.findByMbid(related.mbid());

            Artist similarArtist = optionalArtist.orElseGet(() -> artistService.saveBasic(related.name(), related.mbid()));

            // check cuál tiene el mbid más chico para guardar con consistencia
            if (artist.getMbid().compareTo(similarArtist.getMbid()) < 0){
                artistA = artist;
                artistB = similarArtist;
            } else {
                artistA = similarArtist;
                artistB = artist;
            }

            relation = searchSavedRelation(artistA, artistB);
            if (relation.isPresent()){
                checkAndUpdateWeight(relation.get(), related.weight());
            } else {
                saveRelation(artistA, artistB, related.weight());
            }

        }
    }

    private Optional<ApiArtistRelation> searchSavedRelation(Artist artistA, Artist artistB){

        return relationRepository.findByArtists(artistA, artistB);
    }

    private void saveRelation(Artist artistA, Artist artistB, double weight){
        ApiArtistRelation apiArtistRelation = new ApiArtistRelation();
        apiArtistRelation.setArtistA(artistA);
        apiArtistRelation.setArtistB(artistB);
        apiArtistRelation.setWeight((int) (weight * 100));

        relationRepository.save(apiArtistRelation);
    }

    private void checkAndUpdateWeight(ApiArtistRelation relation, double weight){
        Integer newWeight = (int) (weight * 100);

        if (relation.getWeight() >= newWeight){
            return;
        }

        relation.setWeight(newWeight);
        relationRepository.save(relation);
    }
}