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
@Table(name = "user_tags")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserTag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne
    User user;

    @NotNull
    String name;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;
}
