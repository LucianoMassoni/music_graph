package com.luciano.music_graph.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;


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

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_artist_relation_tags",
            joinColumns = @JoinColumn(name = "relation_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<UserTag> userTags;
}
