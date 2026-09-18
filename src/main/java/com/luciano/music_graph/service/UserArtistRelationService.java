package com.luciano.music_graph.service;


import com.luciano.music_graph.dto.graph.Link;
import com.luciano.music_graph.mapper.UserArtistRelationMapper;
import com.luciano.music_graph.model.*;
import com.luciano.music_graph.repository.UserArtistRelationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class UserArtistRelationService {

    private final UserArtistRelationRepository userArtistRelationRepository;
    private final UserArtistRelationMapper mapper;

    @Value("${graph.tag-weight}")
    private Integer TAG_WEIGHT;

    // cada vez que se llama a seguir o dejar de seguir artista se recalcula el peso entre artistas.
    public void recalculateFromTags(User user, Artist artist, UserTag userTag){

        Artist artistA;
        Artist artistB;

        // la base de datos devuelve una lista de objetos que tiene [Artist, Long] por cada línea.
        List<Object[]> results = userArtistRelationRepository.getUserArtistTagByUserAndArtist(user, artist);

        for (Object[] row : results) {
            Artist relatedArtist = (Artist) row[0];
            Long sharedTags = (Long) row[1];

            // check cuál es el más chico.
            if (artist.getMbid().compareTo(relatedArtist.getMbid()) < 0){
                artistA = artist;
                artistB = relatedArtist;
            } else {
                artistA = relatedArtist;
                artistB = artist;
            }

            // la de tags en común por el peso de cercanía.
            Integer weight = sharedTags.intValue() * TAG_WEIGHT;

            UserArtistRelation userArtistRelation = getOrCreate(user, artistA, artistB, userTag);
            userArtistRelation.setWeight(weight);

            userArtistRelationRepository.save(userArtistRelation);
        }
    }


    private UserArtistRelation getOrCreate(User user, Artist artistA, Artist artistB, UserTag userTag){

        UserArtistRelation relation = userArtistRelationRepository.getEntityByUserAndArtist(user, artistA, artistB)
                    .orElseGet(() ->
                            mapper.toEntity(
                                    user,
                                    artistA,
                                    artistB,
                                    new ArrayList<>()
                            )
                    );

        if (!relation.getUserTags().contains(userTag)) {
            relation.getUserTags().add(userTag);
        }

        return relation;
    }

    @Transactional
    public void checkAndDeleteRelation(User user, Artist artist, UserTag userTag){

        List<UserArtistRelation> relations = userArtistRelationRepository.getRelationsByUserArtistAndTag(user, artist, userTag);

        relations.forEach(relation -> {
            relation.getUserTags().remove(userTag);

            int weight = relation.getUserTags().size() * TAG_WEIGHT;

            if (weight == 0) {
                userArtistRelationRepository.delete(relation);
            } else {
                relation.setWeight(weight);
                userArtistRelationRepository.save(relation);
            }
        });
    }

    public List<Link> getLinks(User user, String mbid){

        List<Link> links = new ArrayList<>();

        List<Object[]> relations = userArtistRelationRepository.getAllRelations(user, mbid);

        for (Object[] row : relations){
            String artistAMbid = row[0].toString();
            String artistBMbid = row[1].toString();
            int weight = (int) row[2];
            boolean active = (boolean) row[3];

            links.add(new Link(
                    artistAMbid,
                    artistBMbid,
                    weight,
                    active
            ));
        }

        return links;
    }

}
