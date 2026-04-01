package com.luciano.music_graph.repository;

import com.luciano.music_graph.model.ApiArtistRelation;
import com.luciano.music_graph.model.Artist;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiArtistRelationRepository extends JpaRepository<ApiArtistRelation, UUID> {
    @Query("""
        select ar
        from ApiArtistRelation ar
        where ar.artistA = :artistA and ar.artistB = :artistB
    """)
    Optional<ApiArtistRelation> findByArtists(Artist artistA, Artist artistB);

    @Query("""
        select ar
        from ApiArtistRelation as ar
        where ar.artistA.mbid = :mbid or ar.artistB.mbid = :mbid
        order by ar.weight desc
    """)
    List<ApiArtistRelation> findRelatedByMbid(String mbid, Pageable pageable);


    @Query("""
        select ar
        from ApiArtistRelation as ar
        where ar.artistA.id in (:artistsIds)
        and ar.artistB.id in (:artistsIds)
    """)
    List<ApiArtistRelation> findRelations(List<UUID> artistsIds);
}
