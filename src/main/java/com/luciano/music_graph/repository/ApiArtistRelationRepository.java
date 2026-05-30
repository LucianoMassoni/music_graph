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

    @Query("""
    select
        artist_a.mbid as artist_a_mbid,
        artist_b.mbid as artist_b_mbid,
        aar.weight
    
    from ApiArtistRelation aar
    
    join Artist artist_a
        on artist_a.id = aar.artistA.id
    
    join Artist artist_b
        on artist_b.id = aar.artistB.id
    
    where (
        artist_a.mbid = :mbid
        or artist_b.mbid = :mbid
    ) and aar.weight >= 50
    """)
    List<Object[]> getAllRelated(String mbid);
}
