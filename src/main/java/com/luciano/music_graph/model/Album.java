package com.luciano.music_graph.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "album")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String mbid;

    @NotBlank
    private String name;

    @ManyToOne
    private Artist artist;

    private int year;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "lastfm_url")
    private String lastfmUrl;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
