package com.luciano.music_graph.repository;

import com.luciano.music_graph.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlbumRepository extends JpaRepository<Album, UUID> {
    List<Album> findAllByArtistId(UUID artistId);
}
