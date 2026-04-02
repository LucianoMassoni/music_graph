package com.luciano.music_graph.repository;

import com.luciano.music_graph.model.User;
import com.luciano.music_graph.model.UserTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserTagRepository extends JpaRepository<UserTag, UUID> {

    @Query("""
        select ut
        from UserTag as ut
        where upper(ut.name) = upper(:name) and ut.user = :user
    """)
    List<UserTag> findByUserTagName(User user, String name);

    List<UserTag> findByUser(User user);

}
