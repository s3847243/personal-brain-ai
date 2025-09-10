package com.example.personalbrain.ingestion.service;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;
@Service
@RequiredArgsConstructor
public class OpenAiEmbeddingService {

    @Value("${openai.api-key}")
    private String apiKey;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    public List<Float> getEmbedding(String input) {
        Map<String, Object> body = Map.of(
                "input", input,
                "model", "text-embedding-3-small"
        );

        Map<String, Object> response = webClient.post()
                .uri("/embeddings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        if (data.isEmpty()) throw new RuntimeException("No embeddings returned");

        Map<String, Object> first = data.get(0);
        List<Double> doubleList = (List<Double>) first.get("embedding");

        // ✅ Convert to List<Float> safely
        return doubleList.stream()
                .map(Double::floatValue)
                .collect(Collectors.toList());
    }
}