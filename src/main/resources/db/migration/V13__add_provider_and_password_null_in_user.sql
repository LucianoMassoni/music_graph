alter table users
add column auth_provider varchar(60),
alter column password_hash drop not null;