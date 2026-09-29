package com.back.nbe12142team06.global.restclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Anthropic Claude API 전용 RestClient.
 * 테스트 프로파일에서는 실제 API 를 호출하지 않으므로 빈을 만들지 않는다.
 */
@Configuration
@Profile("!test")
public class ClaudeRestClientConfig {

    @Value("${custom.claude.api-key}")
    private String apiKey;

    @Bean
    public RestClient claudeRestClient() {

        // 외부 API 는 언제든 느려질 수 있으므로 타임아웃을 반드시 건다.
        // 타임아웃이 없으면 응답이 올 때까지 우리 스레드가 묶여 서비스 전체가 같이 느려진다.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30));

        return RestClient.builder()
                .baseUrl("https://api.anthropic.com")
                .requestFactory(factory)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}