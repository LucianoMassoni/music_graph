package com.luciano.music_graph.config;

import com.luciano.music_graph.exception.LastFmArtistNotFoundException;
import com.luciano.music_graph.exception.LastFmApiException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean("lastFmRestClient")
    public RestClient lastFmRestClient(){
        return RestClient.builder()
                .baseUrl("https://ws.audioscrobbler.com/2.0/")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, ((request, response) -> {
                    throw new LastFmApiException("server error");
                }))
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, ((request, response) -> {
                    if (response.getStatusCode().value() == 404){
                        throw new LastFmArtistNotFoundException("Artist not found exception.");
                    }
                    throw new LastFmApiException("Client error");
                }))
                .build();

    }
}
