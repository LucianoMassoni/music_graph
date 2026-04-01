create table user_artist_tags (
    id uuid not null,
    user_id uuid not null,
    artist_id uuid not null,
    user_tag_id uuid not null,
    primary key (id),
    foreign key (user_id) references users(id),
    foreign key (artist_id) references artist(id),
    foreign key (user_tag_id) references user_tags(id),
    constraint uq_same_tag_per_artist unique (user_tag_id, artist_id)
);