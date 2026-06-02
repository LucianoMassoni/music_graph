 package com.luciano.music_graph.controller;

import com.luciano.music_graph.dto.graph.GraphResponse;
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
    public ResponseEntity<GraphResponse> library(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(graphService.getLibraryGraph(user));
    }

    @GetMapping("/discovery/{mbid}")
    public ResponseEntity<GraphResponse> discovery(@AuthenticationPrincipal User user, @PathVariable String mbid){
        return ResponseEntity.ok(graphService.getDiscoveryGraph(user, mbid));
    }
}
