create table user_artist_relation_tags (
    relation_id uuid not null,
    tag_id uuid not null,
    primary key (relation_id, tag_id),
    
    foreign key (relation_id)
        references user_artist_relations(id) on delete cascade,
        
    foreign key (tag_id)
        references user_tags(id) on delete cascade
);
