package com.luciano.music_graph.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_artist_tags")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserArtistTag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne
    User user;

    @ManyToOne
    Artist artist;

    @ManyToOne
    @JoinColumn(name = "user_tag_id")
    UserTag userTag;
}
