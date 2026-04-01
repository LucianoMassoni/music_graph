package com.luciano.music_graph.repository;

import com.luciano.music_graph.model.UserArtistTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserArtistTagRepository extends JpaRepository<UserArtistTag, UUID> {
}
