package com.luciano.music_graph;

import com.luciano.music_graph.client.LastFmClient;
import com.luciano.music_graph.service.JwtService;
import com.luciano.music_graph.service.RefreshTokenService;
import com.luciano.music_graph.utils.CookieUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class MusicGraphApplicationTests extends BaseIntegrationTest {

	@MockitoBean
	private LastFmClient lastFmClient;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private RefreshTokenService refreshTokenService;

	@MockitoBean
	private CookieUtils cookieUtils;

	@Test
	void contextLoads() {
	}

}
