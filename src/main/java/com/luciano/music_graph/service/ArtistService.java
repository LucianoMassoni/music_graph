package com.luciano.music_graph.service;

import com.luciano.music_graph.client.LastFmClient;
import com.luciano.music_graph.dto.*;
import com.luciano.music_graph.dto.lastfm.LFAlbumResponse;
import com.luciano.music_graph.dto.lastfm.LFArtistInfoResponse;
import com.luciano.music_graph.dto.lastfm.LFSearchResponse;
import com.luciano.music_graph.dto.lastfm.LFTopTagsResponse;
import com.luciano.music_graph.mapper.ArtistMapper;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.repository.ArtistRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final LastFmClient lastFmClient;
    private final ArtistRepository artistRepository;
    private final ArtistMapper mapper;
    private final ArtistTagService tagService;
    private final AlbumService albumService;

    public ArtistSearchResult search(String name){
        LFSearchResponse response = lastFmClient.search(name);

        Set<String> seen = new HashSet<>();

        List<ArtistSearchData> lista = response
                .results()
                .artistmatches()
                .artist().stream()
                .filter(artist -> !artist.mbid().isEmpty())
                .filter(artist -> seen.add(artist.mbid()))
                .map(mapper::toArtistSearchData)
                .toList();

        return new ArtistSearchResult(lista);
    }

    public ArtistDetail getOrImport(String mbid){
        Artist artist = artistRepository.findByMbid(mbid).orElseGet(() -> importArtist(mbid));

        List<ArtistTagData> artistTagDataList = tagService.getAllTagDataByArtistId(artist.getId());
        List<AlbumDetail> albumDetailList = albumService.getAllAlbumDataByArtistId(artist.getId());

        return mapper.toArtistDetail(artist, artistTagDataList, albumDetailList);
    }

    @Transactional
    private Artist importArtist(String mbid){
        // llamo al traer el artista y lo guarda en db
        LFArtistInfoResponse artistInfoResponse = lastFmClient.getInfo(mbid);
        Artist artist = artistRepository.save(mapper.toEntity(artistInfoResponse.artist()));

        // llama a los top tags del artista y los guarda
        LFTopTagsResponse tagResponse = lastFmClient.getTopTags(mbid);
        tagService.saveAllTagsInArtist(tagResponse.toptags(), artist);

        // llama a los albums del artista y los guarda
        LFAlbumResponse albumResponse = lastFmClient.getAlbums(mbid);
        albumService.saveAllAlbumInArtist(albumResponse.topalbums(), artist);

        return artist;
    }
}
