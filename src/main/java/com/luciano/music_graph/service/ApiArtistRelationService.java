package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.ApiArtistRelationResponse;
import com.luciano.music_graph.dto.ArtistRelatedDto;
import com.luciano.music_graph.dto.RelationEdge;
import com.luciano.music_graph.dto.lastfm.LFSimilarArtistResponse;
import com.luciano.music_graph.mapper.ApiArtistRelationMapper;
import com.luciano.music_graph.model.ApiArtistRelation;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.repository.ApiArtistRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiArtistRelationService {

    private final ApiArtistRelationRepository relationRepository;
    private final ApiArtistRelationMapper mapper;
    private final ArtistService artistService;

    public ApiArtistRelationResponse buidApiRelations(Artist artist, LFSimilarArtistResponse similarArtistResponse){

        Artist artistA;
        Artist artistB;
        Optional<ApiArtistRelation> relation;

        List<ArtistRelatedDto> artistRelatedDtos = mapper.toArtistRelatedList(similarArtistResponse);
        List<ApiArtistRelation> lista = new ArrayList<>();

        for (ArtistRelatedDto related : artistRelatedDtos){
            if (related.mbid() == null) continue;

            Optional<Artist> optionalArtist = artistService.findByMbid(related.mbid());

            Artist similarArtist = optionalArtist.orElseGet(() -> artistService.saveBasic(related.name(), related.mbid()));


            if (artist.getId().toString().compareTo(similarArtist.getId().toString()) < 0){
                artistA = artist;
                artistB = similarArtist;
            } else {
                artistA = similarArtist;
                artistB = artist;
            }

            relation = searchSavedRelation(artistA, artistB);
            if (relation.isPresent()){
                ApiArtistRelation relatedArtist = checkAndUpdateWeight(relation.get(), related.weight());
                lista.add(relatedArtist);
            } else {
                lista.add(saveRelation(artistA, artistB, related.weight()));
            }

        }

        return new ApiArtistRelationResponse(
                mapper.toShortArtistInfoDto(artist),
                lista.stream().map(r -> mapper.toRelatedDto(r, artist)).toList()
        );
    }

    private Optional<ApiArtistRelation> searchSavedRelation(Artist artistA, Artist artistB){

        return relationRepository.findByArtists(artistA, artistB);
    }

    private ApiArtistRelation saveRelation(Artist artistA, Artist artistB, double weight){
        ApiArtistRelation apiArtistRelation = new ApiArtistRelation();
        apiArtistRelation.setArtistA(artistA);
        apiArtistRelation.setArtistB(artistB);
        apiArtistRelation.setWeight((int) (weight * 100));

        return relationRepository.save(apiArtistRelation);
    }

    private ApiArtistRelation checkAndUpdateWeight(ApiArtistRelation relation, double weight){
        Integer newWeight = (int) (weight * 100);

        if (relation.getWeight() >= newWeight){
            return relation;
        }

        relation.setWeight(newWeight);
        return relationRepository.save(relation);
    }

    public RelationEdge getApiArtistRelations(List<Artist> artists){

        List<ApiArtistRelation> artistRelations = relationRepository.findRelations(artists.stream().map(Artist::getId).toList());

        return mapper.toRelationEdge(artistRelations);
    }

    public List<ApiArtistRelation> getRelatedArtists(String mbid, int limit){

        return relationRepository.findRelatedByMbid(mbid, PageRequest.of(0, limit));
    }
}