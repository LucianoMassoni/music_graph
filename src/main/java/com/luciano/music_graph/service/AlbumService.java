package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.AlbumDetail;
import com.luciano.music_graph.dto.lastfm.LFTopAlbums;
import com.luciano.music_graph.mapper.AlbumMapper;
import com.luciano.music_graph.model.Album;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumMapper mapper;

    public List<AlbumDetail> getAllAlbumDataByArtistId(UUID artistId){
        List<Album> albums = albumRepository.findAllByArtistId(artistId);
        return albums.stream().map(mapper::toAlbumData).toList();
    }

    public void saveAllAlbumInArtist(LFTopAlbums albums, Artist artist){
        albumRepository.saveAll(albums.album().stream().map(album -> mapper.toEntity(album, artist)).filter(album -> album.getMbid() != null).toList());
    }
}
