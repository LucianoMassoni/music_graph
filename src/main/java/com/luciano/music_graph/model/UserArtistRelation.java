package com.luciano.music_graph.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "user_artist_relations")

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class UserArtistRelation extends ArtistRelationBase {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
