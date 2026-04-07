package com.luciano.music_graph.service;

import com.luciano.music_graph.client.LastFmClient;
import com.luciano.music_graph.dto.lastfm.*;
import com.luciano.music_graph.exception.LastFmApiException;
import com.luciano.music_graph.exception.LastFmArtistNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;


public class LastFmClientTest {

    private LastFmClient lastFmClient;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {

        // copio mi RestClient de lastfm pero en vez de ir a la url original va a una falsa; localhost
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost")
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, ((request, response) -> {
                    throw new LastFmApiException("server error");
                }))
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, ((request, response) -> {
                    if (response.getStatusCode().value() == 404){
                        throw new LastFmArtistNotFoundException("Artist not found exception.");
                    }
                    throw new LastFmApiException("Client error");
                }));

        server = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.build();

        lastFmClient = new LastFmClient(restClient);
        ReflectionTestUtils.setField(lastFmClient, "API_KEY", "test_key");
    }



    // HELPER
    // para testear las funciones que traen dato de igual manera
    private void mockResponse(String expectedMethod, String json) {
        server.expect(requestTo(allOf(
                        containsString("method=" + expectedMethod),
                        containsString("api_key=test_key")
                )))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }


    @Test
    void search_shouldReturnMappedResponse() {

        String artistName = "radiohead";

        String json = """
        {
          "results": {
            "artistmatches": {
              "artist": [
                {
                  "name": "Radiohead",
                  "mbid": "123"
                }
              ]
            }
          }
        }
        """;

        server.expect(requestTo(allOf(
                containsString("artist.search"),
                containsString("artist=radiohead"),
                containsString("api_key=test_key")
        )))
        .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        LFSearchResponse response = lastFmClient.search(artistName);

        assertNotNull(response);
        assertEquals("Radiohead",
                response.results()
                        .artistmatches()
                        .artist()
                        .getFirst()
                        .name()
        );
    }

    @Test
    void getInfo_shouldReturnMappedResponse() {

        String json = """
        {
          "artist": {
            "name": "Radiohead",
            "mbid": "123"
          }
        }
        """;

        mockResponse("artist.getinfo", json);

        LFArtistInfoResponse response = lastFmClient.getInfo("123");

        assertNotNull(response);
        assertEquals("Radiohead", response.artist().name());
    }

    @Test
    void getTopTags_shouldReturnMappedResponse() {

        String json = """
        {
          "toptags": {
            "tag": [
              { "name": "rock", "count": 100 }
            ]
          }
        }
        """;

        mockResponse("artist.getTopTags", json);

        LFTopTagsResponse response = lastFmClient.getTopTags("123");

        assertNotNull(response);
        assertEquals("rock", response.toptags().tag().getFirst().name());
    }

    @Test
    void getAlbums_shouldReturnMappedResponse() {

        String json = """
        {
          "topalbums": {
            "album": [
              { "name": "OK Computer" }
            ]
          }
        }
        """;

        mockResponse("artist.getTopAlbums", json);

        LFAlbumResponse response = lastFmClient.getAlbums("123");

        assertNotNull(response);
        assertEquals("OK Computer", response.topalbums().album().getFirst().name());
    }

    @Test
    void getInfo_shouldThrowLastFmApiException_whenServerError() {

        server.expect(anything())
                .andRespond(withServerError());

        assertThrows(LastFmApiException.class, () -> {
            lastFmClient.getInfo("123");
        });
    }

    @Test
    void getInfo_shouldThrowNotFoundException_when404() {

        server.expect(anything())
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(LastFmArtistNotFoundException.class, () -> {
            lastFmClient.getInfo("123");
        });
    }

    @Test
    void getInfo_shouldHandleMissingFields() {

        String json = """
        {
          "artist": {
            "name": "Radiohead"
          }
        }
        """;

        mockResponse("artist.getinfo", json);

        LFArtistInfoResponse response = lastFmClient.getInfo("123");

        assertNotNull(response);
        assertNotNull(response.artist());
        assertEquals("Radiohead", response.artist().name());

        // esto es lo importante
        assertNull(response.artist().mbid());
    }

    @Test
    void getSimilar_shouldReturnMappedResponse() {

        String json = """
        {
          "similarartists": {
            "artist": [
              {
                "name": "Muse",
                "mbid": "456"
              }
            ]
          }
        }
        """;

        mockResponse("artist.getSimilar", json);

        LFSimilarArtistResponse response = lastFmClient.getSimilar("123");

        assertNotNull(response);
        assertNotNull(response.similarartists());
        assertEquals(1, response.similarartists().artist().size());
        assertEquals("Muse", response.similarartists().artist().getFirst().name());
    }

    @Test
    void getSimilar_shouldHandleEmptyList() {

        String json = """
        {
          "similarartists": {
            "artist": []
          }
        }
        """;

        mockResponse("artist.getSimilar", json);

        LFSimilarArtistResponse response = lastFmClient.getSimilar("123");

        assertNotNull(response);
        assertNotNull(response.similarartists());
        assertTrue(response.similarartists().artist().isEmpty());
    }

    @Test
    void getSimilar_shouldHandleMissingArtistField() {

        String json = """
        {
          "similarartists": {}
        }
        """;

        mockResponse("artist.getSimilar", json);

        LFSimilarArtistResponse response = lastFmClient.getSimilar("123");

        assertNotNull(response);
        assertNotNull(response.similarartists());

        assertNull(response.similarartists().artist());
    }

}
