package com.luciano.music_graph.repository;

import com.luciano.music_graph.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ArtistRepository extends JpaRepository<Artist, UUID> {
    Optional<Artist> findByMbid(String mbid);
}
