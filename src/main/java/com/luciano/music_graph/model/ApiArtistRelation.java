package com.luciano.music_graph.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "api_artist_relations")

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ApiArtistRelation extends ArtistRelationBase {
}
