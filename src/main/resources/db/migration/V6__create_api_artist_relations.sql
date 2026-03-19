create table api_artist_relations(
    id uuid not null,
    artist_a_id uuid not null,
    artist_b_id uuid not null,
    weight int,
    created_at timestamp(6),
    primary key (id),
    foreign key (artist_a_id) references artist(id),
    foreign key (artist_b_id) references artist(id),
    constraint chk_artist_order check ( artist_a_id < artist_b_id ),
    constraint uq_api_artist_relation unique( artist_a_id, artist_b_id )
);