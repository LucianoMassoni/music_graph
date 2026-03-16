package com.luciano.music_graph.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue( strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expires_at")
    private Instant expiresAt;

    private boolean revoked;

    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;
}
