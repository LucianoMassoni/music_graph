package com.luciano.music_graph.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "artist")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, name = "mbid")
    private String mbid;

    @Column(nullable = false)
    private String name;

    private String country;

    @Column(name = "debut_year")
    private Integer debutYear;

    private String type;

    private String disambiguation;

    private String bio;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    private ArtistSource source;

    @NotNull
    private boolean enriched;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

}
