package com.luciano.music_graph.repository;

import com.luciano.music_graph.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    @Query("""
    update RefreshToken r
    set r.revoked = true
    where r.token = :token
    """)
    @Modifying(clearAutomatically = true)
    void revokeToken(@Param("token") String token);

    Optional<RefreshToken> findByToken(String token);
}
