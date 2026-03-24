package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.ArtistTagData;
import com.luciano.music_graph.dto.lastfm.LFTopTags;
import com.luciano.music_graph.mapper.ArtistTagMapper;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.ArtistTag;
import com.luciano.music_graph.repository.ArtistTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistTagService {

    private final ArtistTagRepository artistTagRepository;
    private final ArtistTagMapper mapper;


    public List<ArtistTagData> getAllTagDataByArtistId(UUID artistId){

        List<ArtistTag> lista = artistTagRepository.findAllByArtistId(artistId);

        return lista.stream().map(mapper::toArtistTagData).toList();
    }

    public void saveAllTagsInArtist(LFTopTags topTags, Artist artist){
        artistTagRepository.saveAll(topTags.tag().stream().map(lfTag -> mapper.toEntity(lfTag, artist)).toList());
    }
}