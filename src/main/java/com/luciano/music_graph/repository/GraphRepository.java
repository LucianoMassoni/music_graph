package com.luciano.music_graph.repository;

import com.luciano.music_graph.model.ApiArtistRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;


public interface GraphRepository extends JpaRepository<ApiArtistRelation, UUID> {

    @Query(value = """
    select
        a.mbid as artist_a_mbid,
        b.mbid as artist_b_mbid,
        coalesce(aar.weight, 0) + coalesce(uar.weight, 0) as combined_weight
    from api_artist_relations aar
    full outer join user_artist_relations uar
        on aar.artist_a_mbid = uar.artist_a_mbid
        and aar.artist_b_mbid = uar.artist_b_mbid
        and uar.user_id = :userId
    join artist a on a.mbid = coalesce(aar.artist_a_mbid, uar.artist_a_mbid)
    join artist b on b.mbid = coalesce(aar.artist_b_mbid, uar.artist_b_mbid)
    where a.id in (select artist_id from user_artists where user_id = :userId and followed = true)
    and b.id in (select artist_id from user_artists where user_id = :userId and followed = true)
    """, nativeQuery = true)
    List<Object[]> getCombinedRelationsForLibrary(UUID userId);

}
