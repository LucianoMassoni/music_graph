package com.luciano.music_graph.controller;

import com.luciano.music_graph.dto.userArtistTag.ArtistsByTagResponse;
import com.luciano.music_graph.dto.userArtistTag.UserArtistTagResponse;
import com.luciano.music_graph.dto.userTag.TagRequest;
import com.luciano.music_graph.dto.userTag.TagResponse;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.service.UserArtistTagService;
import com.luciano.music_graph.service.UserTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final UserTagService userTagService;
    private final UserArtistTagService userArtistTagService;

    @PostMapping
    public ResponseEntity<TagResponse> create(@AuthenticationPrincipal User user, @RequestBody TagRequest request){
        return ResponseEntity.ok(userTagService.create(user, request));
    }

    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(userTagService.getAllUserTags(user));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID tagId){

        userTagService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tagId}/artists/{artistMbid}")
    public ResponseEntity<UserArtistTagResponse> create(@AuthenticationPrincipal User user, @PathVariable UUID tagId, @PathVariable String artistMbid){
        return ResponseEntity.ok(userArtistTagService.create(user, tagId,artistMbid));
    }

    @DeleteMapping("/{tagId}/artists/{artistMbid}")
    public ResponseEntity<Void> delete(@PathVariable UUID tagId, @PathVariable String artistMbid){

        userArtistTagService.delete(tagId, artistMbid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tagId}/artists")
    public ResponseEntity<ArtistsByTagResponse> getArtistsByTag(@PathVariable UUID tagId){
        return ResponseEntity.ok(userArtistTagService.getArtistsByTagId(tagId));
    }

}
