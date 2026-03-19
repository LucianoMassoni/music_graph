package com.luciano.music_graph.client;

import com.luciano.music_graph.dto.lastfm.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Component
public class LastFmClient {

    @Qualifier("lastFmRestClient")
    private final RestClient lastFmRestClient;

    @Value("${lastfm.api-key}")
    private String API_KEY;


    public LFSearchResponse search(String artistName){

        return lastFmRestClient.get()
                .uri("?method=artist.search&artist={artistName}&api_key={API_KEY}&format=json", artistName, API_KEY)
                .retrieve()
                .body(LFSearchResponse.class);
    }

    public LFArtistInfoResponse getInfo(String mbid){

        return lastFmRestClient.get()
                .uri("?method=artist.getinfo&mbid={mbid}&api_key={API_KEY}&format=json", mbid, API_KEY)
                .retrieve()
                .body(LFArtistInfoResponse.class);
    }

    public LFSimilarArtistResponse getSimilar(String mbid){

        return lastFmRestClient.get()
                .uri("?method=artist.getSimilar&mbid={mbid}&api_key={API_KEY}&format=json", mbid, API_KEY)
                .retrieve()
                .body(LFSimilarArtistResponse.class);
    }

    public LFTopTagsResponse getTopTags(String mbid){

        return lastFmRestClient.get()
                .uri("?method=artist.getTopTags&mbid={mbid}&api_key={API_KEY}&format=json", mbid, API_KEY)
                .retrieve()
                .body(LFTopTagsResponse.class);
    }

    public LFAlbumResponse getAlbums(String mbid){

        return lastFmRestClient.get()
                .uri("?method=artist.getTopAlbums&mbid={mbid}&api_key={API_KEY}&format=json", mbid, API_KEY)
                .retrieve()
                .body(LFAlbumResponse.class);
    }
}
