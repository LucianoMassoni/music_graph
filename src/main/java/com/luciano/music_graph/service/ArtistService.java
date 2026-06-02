package com.luciano.music_graph.service;

import com.luciano.music_graph.client.LastFmClient;
import com.luciano.music_graph.dto.*;
import com.luciano.music_graph.dto.lastfm.LFAlbumResponse;
import com.luciano.music_graph.dto.lastfm.LFArtistInfoResponse;
import com.luciano.music_graph.dto.lastfm.LFImageItem;
import com.luciano.music_graph.dto.lastfm.LFTopTagsResponse;
import com.luciano.music_graph.mapper.ArtistMapper;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.repository.ArtistRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final LastFmClient lastFmClient;
    private final ArtistRepository artistRepository;
    private final ArtistMapper mapper;
    private final ArtistTagService tagService;
    private final AlbumService albumService;


    public ArtistDetail getOrImport(String mbid){
        Artist artist = artistRepository.findByMbid(mbid).orElseGet(() -> importArtist(mbid));

        if (artist.getBio() == null){
            enrichArtist(artist);
        }

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

    private void enrichArtist(Artist artist){

        String mbid = artist.getMbid();

        LFArtistInfoResponse artistInfoResponse = lastFmClient.getInfo(mbid);

        artist.setBio(artistInfoResponse.artist().bio().content());
        artist.setImageUrl(artistInfoResponse.artist().image().stream()
                .filter(img -> "extralarge".equals(img.size()))
                .map(LFImageItem::text)
                .findFirst()
                .orElse(null));

        // llama a los top tags del artista y los guarda
        LFTopTagsResponse tagResponse = lastFmClient.getTopTags(mbid);
        tagService.saveAllTagsInArtist(tagResponse.toptags(), artist);

        // llama a los albums del artista y los guarda
        LFAlbumResponse albumResponse = lastFmClient.getAlbums(mbid);
        albumService.saveAllAlbumInArtist(albumResponse.topalbums(), artist);

        artistRepository.save(artist);
    }

    public Optional<Artist> findByMbid(String mbid) {
        return artistRepository.findByMbid(mbid);
    }

    public Artist saveBasic(String name, String mbid){
        Artist artist = new Artist();
        artist.setName(name);
        artist.setMbid(mbid);
        return artistRepository.save(artist);
    }
}
