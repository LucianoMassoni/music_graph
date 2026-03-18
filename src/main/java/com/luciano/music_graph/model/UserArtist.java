package com.luciano.music_graph.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_artists")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserArtist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    private Artist artist;

    private boolean followed = false;

    @Column(name = "followed_at")
    private Instant followedAt;
}
