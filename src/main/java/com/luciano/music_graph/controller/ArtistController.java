package com.luciano.music_graph.controller;

import com.luciano.music_graph.dto.ArtistSearchResult;
import com.luciano.music_graph.dto.UserArtistDetail;
import com.luciano.music_graph.dto.UserArtistResponse;
import com.luciano.music_graph.dto.userTag.TagResponse;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.service.ArtistService;
import com.luciano.music_graph.service.UserArtistService;
import com.luciano.music_graph.service.UserArtistTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/artists")
public class ArtistController {

    private final ArtistService artistService;
    private final UserArtistService userArtistService;
    private final UserArtistTagService userArtistTagService;

    @GetMapping("/search")
    public ResponseEntity<ArtistSearchResult> search(@AuthenticationPrincipal User user, @RequestParam String q){
        return ResponseEntity.ok(userArtistService.search(user, q));
    }

    @GetMapping("/{mbid}")
    public ResponseEntity<UserArtistDetail> getByMbid(@AuthenticationPrincipal User user, @PathVariable String mbid){
        return ResponseEntity.ok(userArtistService.getArtist(user, mbid));
    }

    @PostMapping("/{mbid}/follow")
    public ResponseEntity<Void> followArtist(@AuthenticationPrincipal User user, @PathVariable String mbid){

        userArtistService.followArtist(user, mbid);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{mbid}/follow")
    public ResponseEntity<Void> unfollowArtist(@AuthenticationPrincipal User user, @PathVariable String mbid){

        userArtistService.unfollowArtist(user, mbid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followed")
    public ResponseEntity<UserArtistResponse> getAllFollowed(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(userArtistService.getAllFollowed(user));
    }

//    @GetMapping("/{mbid}/tags")
//    public ResponseEntity<TagsByArtistResponse> getTagByArtist(@PathVariable String mbid){
//        return ResponseEntity.ok(userArtistTagService.getTagsByArtistMbid(mbid));
//    }

    @GetMapping("/{mbid}/tags")
    public ResponseEntity<List<TagResponse>> getTagByArtist(@AuthenticationPrincipal User user, @PathVariable String mbid){
        return ResponseEntity.ok(userArtistTagService.getUserTagsByUserAndArtistMbid(user,mbid));
    }
}
