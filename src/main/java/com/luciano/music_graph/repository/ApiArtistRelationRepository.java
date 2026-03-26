package com.luciano.music_graph.repository;

import com.luciano.music_graph.model.ApiArtistRelation;
import com.luciano.music_graph.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ApiArtistRelationRepository extends JpaRepository<ApiArtistRelation, UUID> {
    @Query("""
        select ar
        from ApiArtistRelation ar
        where ar.artistA = :artistA and ar.artistB = :artistB
    """)
    Optional<ApiArtistRelation> findByArtists(Artist artistA, Artist artistB);
}
