package com.luciano.music_graph.repository;

import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.UserArtistTag;
import com.luciano.music_graph.model.UserTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserArtistTagRepository extends JpaRepository<UserArtistTag, UUID> {

    @Query("""
        select uat
        from UserArtistTag as uat
        where uat.userTag.id = :tagId and uat.artist.mbid = :artistMbid
    """)
    Optional<UserArtistTag> findByTagIdAndArtistMbid(UUID tagId, String artistMbid);

    @Query("""
        select uat.artist
        from UserArtistTag as uat
        where uat.userTag.id = :tagId
    """)
    List<Artist> findArtistByTagId(UUID tagId);

    @Query("""
        select uat.userTag
        from UserArtistTag as uat
        where uat.artist.mbid = :mbid
    """)
    List<UserTag> findByArtistMbid(String mbid);
}
