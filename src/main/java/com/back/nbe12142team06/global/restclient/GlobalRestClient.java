package com.back.nbe12142team06.global.restclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class GlobalRestClient {
    private final String TOSS_SECRET_KEY = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";

    @Bean
    public RestClient tossRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.tosspayments.com")
                .defaultHeader("Authorization",
                        "Basic " + new String(Base64.getEncoder()
                                .encode((TOSS_SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8))))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
