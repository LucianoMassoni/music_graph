package com.luciano.music_graph.repository;

import com.luciano.music_graph.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserArtistRelationRepository extends JpaRepository<UserArtistRelation, UUID> {

    @Query("""
        select uat2.artist, count(*) as shared_tags
        from UserArtistTag uat1
        join UserArtistTag uat2 on uat1.userTag = uat2.userTag
        where uat1.user = :user
        and uat1.artist = :artist
        and uat2.artist != :artist
        group by uat2.artist
    """)
    List<Object[]> getUserArtistTagByUserAndArtist(User user, Artist artist);


    @Query("""
        select uar
        from UserArtistRelation as uar
        where uar. user = :user and uar.artistA = :artistA and uar.artistB = :artistB
    """)
    Optional<UserArtistRelation> getEntityByUserAndArtist(User user, Artist artistA, Artist artistB);

    @Query("""
    select
        artist_a.mbid as artist_a_mbid,
        artist_b.mbid as artist_b_mbid,
        aar.weight,
        case when ua_a.followed = true and ua_b.followed = true then true\s
        else false
        end as followed
    from ApiArtistRelation aar
    join Artist artist_a
        on artist_a.id = aar.artistA.id
    join Artist artist_b
        on artist_b.id = aar.artistB.id
    left join UserArtist ua_a
    	on ua_a.artist.id = artist_a.id
    	and ua_a.user = :user
    left join UserArtist ua_b
    	on ua_b.artist.id  = artist_b.id
    	and ua_b.user = :user
    where (
        artist_a.mbid = :mbid
        or artist_b.mbid = :mbid
    )
    and aar.weight >= 50
    """)
    List<Object[]> getAllRelations(User user, String mbid);
}
