package com.luciano.music_graph.repository;

import com.luciano.music_graph.model.UserArtistRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserArtistRelationRepository extends JpaRepository<UserArtistRelation, UUID> {
}
