ALTER TABLE user_artist_tags
DROP CONSTRAINT user_artist_tags_user_tag_id_fkey,
ADD CONSTRAINT user_artist_tags_user_tag_id_fkey
    FOREIGN KEY (user_tag_id)
    REFERENCES user_tags(id)
    ON DELETE CASCADE;