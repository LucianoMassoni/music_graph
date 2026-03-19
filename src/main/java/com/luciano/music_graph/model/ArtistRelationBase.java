package com.luciano.music_graph.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class ArtistRelationBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "artist_a_id")
    private Artist artistA;

    @ManyToOne
    @JoinColumn(name = "artist_b_id")
    private Artist artistB;

    private Integer weight;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
