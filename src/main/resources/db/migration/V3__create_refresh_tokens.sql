create table refresh_token (
    created_at timestamp(6),
    expires_at timestamp(6),
    id uuid not null,
    user_id uuid not null,
    token varchar(100) not null unique,
    revoked boolean default false,
    primary key (id),
    foreign key (user_id) references users(id)
);