create table user_tags(
    id uuid not null,
    user_id uuid not null,
    name varchar(84) not null,
    created_at timestamp(6),
    primary key (id),
    foreign key (user_id) references users(id),
    constraint uq_tag_name_per_user unique (user_id, name)
);