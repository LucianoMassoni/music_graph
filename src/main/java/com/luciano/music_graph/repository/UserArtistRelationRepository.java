package com.luciano.music_graph.repository;

import com.luciano.music_graph.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
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
}
