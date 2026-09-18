create table api_artist_relations(
    id uuid not null,
    artist_a_mbid varchar(124) not null,
    artist_b_mbid varchar(124) not null,
    weight int,
    created_at timestamp(6),
    primary key (id),
    foreign key (artist_a_mbid) references artist(mbid),
    foreign key (artist_b_mbid) references artist(mbid),
    constraint chk_artist_order check ( artist_a_mbid < artist_b_mbid ),
    constraint uq_api_artist_relation unique( artist_a_mbid, artist_b_mbid )
);