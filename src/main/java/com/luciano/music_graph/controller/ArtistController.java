package com.luciano.music_graph.controller;

import com.luciano.music_graph.dto.ArtistDetail;
import com.luciano.music_graph.dto.ArtistSearchResult;
import com.luciano.music_graph.service.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/artists")
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping("/search")
    public ResponseEntity<ArtistSearchResult> search(@RequestParam String q){
        return ResponseEntity.ok(artistService.search(q));
    }

    @GetMapping("/{mbid}")
    public ResponseEntity<ArtistDetail> getByMbid(@PathVariable String mbid){
        return ResponseEntity.ok(artistService.getOrImport(mbid));
    }
}
