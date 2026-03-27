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
}
