create table artist(
    id uuid not null,
    mbid varchar(124) not null,
    name varchar(124) not null,
    country varchar(124),
    debut_year int,
    type varchar (64),
    disambiguation varchar(124),
    created_at timestamp(6),
    primary key (id),
    constraint uq_artist_mbid unique (mbid)
);

create table artist_tags(
    id uuid not null,
    artist_id uuid not null,
    tag varchar(64) not null,
    count int,
    primary key (id),
    foreign key (artist_id) references artist(id)
);