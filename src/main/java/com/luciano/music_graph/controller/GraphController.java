package com.luciano.music_graph.controller;

import com.luciano.music_graph.dto.DiscoveryGraphResponse;
import com.luciano.music_graph.dto.LibraryGraphResponse;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.service.GraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
public class GraphController {

    private final GraphService graphService;

    @GetMapping("/library")
    public ResponseEntity<LibraryGraphResponse> library(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(graphService.getLibraryGraph(user));
    }

    @GetMapping("/discovery/{mbid}")
    public ResponseEntity<DiscoveryGraphResponse> discovery(@AuthenticationPrincipal User user, @PathVariable String mbid, @RequestParam int limit){
        return ResponseEntity.ok(graphService.getDiscoveryGraph(user, mbid, limit));
    }
}
