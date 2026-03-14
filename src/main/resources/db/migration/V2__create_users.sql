create table users (
   created_at timestamp(6),
   updated_at timestamp(6),
   id uuid not null,
   email varchar(100) not null unique,
   password_hash varchar(60) not null,
   role varchar(40) check ((role in ('USER','ADMIN'))) not null,
   username varchar(60) not null unique,
   primary key (id)
)