package com.luciano.music_graph.repository;

import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.model.UserArtist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserArtistRepository extends JpaRepository<UserArtist, UUID> {
    @Query("""
        select ua
        from UserArtist as ua
        where ua.user = :u and ua.artist = :a
    """)
    Optional<UserArtist> findByUserAndArtist(@Param("u") User user, @Param("a") Artist artist);

    @Query("""
        select ua
        from UserArtist as ua
        where ua.user = :u and ua.followed = true
    """)
    List<UserArtist> getAllFollowedByUser(@Param("u") User user);

    @Query("""
        select ua.followed
        from UserArtist as ua
        where ua.user = :user and ua.artist.mbid = :mbid
    """)
    Optional<Boolean> isFollowed(User user, String mbid);

    @Query("""
    select distinct
        related_artist.name,
        related_artist.mbid,
        coalesce(ua.followed, false) as followed
    from Artist base_artist
    join ApiArtistRelation aar
        on base_artist.id in (aar.artistA.id, aar.artistB.id)
    join Artist related_artist
        on related_artist.id = case
            when aar.artistA.id = base_artist.id then aar.artistB.id
            else aar.artistA.id
        end
    left join UserArtist ua
        on ua.artist.id = related_artist.id
        and ua.user = :user
    where base_artist = :artist
    and aar.weight >= 50
    """)
    List<Object[]> getAllNodesByUserAndArtist(User user, Artist artist);
}
