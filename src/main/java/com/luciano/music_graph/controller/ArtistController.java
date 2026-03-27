package com.luciano.music_graph.controller;

import com.luciano.music_graph.dto.ApiArtistRelationResponse;
import com.luciano.music_graph.dto.ArtistDetail;
import com.luciano.music_graph.dto.ArtistSearchResult;
import com.luciano.music_graph.dto.UserArtistResponse;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.service.ArtistService;
import com.luciano.music_graph.service.UserArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/artists")
public class ArtistController {

    private final ArtistService artistService;
    private final UserArtistService userArtistService;

    @GetMapping("/search")
    public ResponseEntity<ArtistSearchResult> search(@RequestParam String q){
        return ResponseEntity.ok(artistService.search(q));
    }

    @GetMapping("/{mbid}")
    public ResponseEntity<ArtistDetail> getByMbid(@PathVariable String mbid){
        return ResponseEntity.ok(artistService.getOrImport(mbid));
    }

    @PostMapping("/{mbid}/follow")
    public ResponseEntity<ApiArtistRelationResponse> followArtist(@AuthenticationPrincipal User user, @PathVariable String mbid){
        return ResponseEntity.ok(userArtistService.followArtist(user, mbid));
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
}
