package com.luciano.music_graph.repository;

import com.luciano.music_graph.model.UserArtist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserArtistRepository extends JpaRepository<UserArtist, UUID> {
}
