create table album(
    id uuid not null,
    artist_id uuid not null,
    mbid varchar(124),
    name varchar(124) not null,
    year int,
    image_url text,
    lastfm_url text,
    created_at timestamp(6),
    primary key (id),
    foreign key (artist_id) references artist(id)
);

alter table artist
rename column mb_id to mbid;

alter table artist
add image_url text,
add bio text;