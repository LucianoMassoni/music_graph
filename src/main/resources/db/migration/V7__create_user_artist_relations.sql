create table user_artist_relations(
    id uuid not null,
    artist_a_id uuid not null,
    artist_b_id uuid not null,
    user_id uuid not null,
    weight int,
    created_at timestamp(6),
    primary key (id),
    foreign key (artist_a_id) references artist(id),
    foreign key (artist_b_id) references artist(id),
    foreign key (user_id) references users(id),
    constraint chk_artist_order check ( artist_a_id < artist_b_id ),
    constraint uq_user_artist_relation unique ( user_id, artist_a_id, artist_b_id )
);