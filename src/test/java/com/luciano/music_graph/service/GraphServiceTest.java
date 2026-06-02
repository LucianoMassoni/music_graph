package com.luciano.music_graph.service;

import com.luciano.music_graph.dto.*;
import com.luciano.music_graph.exception.ArtistNotFoundException;
import com.luciano.music_graph.mapper.GraphMapper;
import com.luciano.music_graph.mapper.GraphMapperImpl;
import com.luciano.music_graph.model.Artist;
import com.luciano.music_graph.model.User;
import com.luciano.music_graph.model.UserArtist;
import com.luciano.music_graph.repository.GraphRepository;
import io.swagger.v3.core.util.ReferenceTypeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GraphServiceTest {

    @InjectMocks
    private GraphService graphService;

    @Mock
    private UserArtistService userArtistService;

    @Mock
    private GraphRepository graphRepository;

    @Mock
    private ArtistService artistService;

    private GraphMapper mapper;

    @BeforeEach
    void setUp(){
        mapper = new GraphMapperImpl();
        ReflectionTestUtils.setField(graphService, "mapper", mapper);
    }



}
