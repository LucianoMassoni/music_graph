create table user_artists(
    id uuid not null,
    user_id uuid not null ,
    artist_id uuid not null,
    followed boolean default false,
    followed_at timestamp(6),
    primary key (id),
    foreign key (user_id) references users(id),
    foreign key (artist_id) references artist(id),
    constraint uq_user_artist unique(user_id, artist_id)
);