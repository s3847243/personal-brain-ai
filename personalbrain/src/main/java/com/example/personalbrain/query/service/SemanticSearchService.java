package com.example.personalbrain.query.service;

import com.example.personalbrain.ingestion.model.Chunk;
import com.example.personalbrain.ingestion.repository.ChunkRepository;
import com.example.personalbrain.ingestion.service.OpenAiEmbeddingService;
import com.example.personalbrain.ingestion.service.PineconeService;
import com.example.personalbrain.query.service.DateRangeExtractor.EpochRange;
import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticSearchService {

    @Value("${openai.api-key}") private String apiKey;
    @Value("${pinecone.index-url}") private String pineconeUrl;

    private final WebClient webClient = WebClient.create();
    private final OpenAiTokenizer tokenizer = new OpenAiTokenizer("gpt-3.5-turbo");
    private final PineconeService pineconeService;
    private final OpenAiEmbeddingService embeddingService;
    private final ChunkRepository chunkRepo;

    public List<String> findRelevantChunks(
            String question,
            UUID userId,
            int topK,
            Optional<EpochRange> epochRangeOpt
    ) {
        // 1) embed the query
        List<Float> vector = embeddingService.getEmbedding(question);
        log.info("Query embedded: " + question);
        // 2) query pinecone (scoped by user + date range)
        var matches = pineconeService.queryVectors(vector, topK, userId, epochRangeOpt);
        log.info("Query matched: " + matches);
        // 3) hydrate chunk texts from DB, preserving Pinecone order
        var idsInOrder = matches.stream()
                .map(m -> UUID.fromString(m.getId()))
                .toList();
        log.info("Chunk IDs in order: " + idsInOrder);
        var chunkById = chunkRepo.findAllById(idsInOrder).stream()
                .collect(Collectors.toMap(Chunk::getId, Function.identity()));
        log.info("Chunks hydrated: " + chunkById);
        return idsInOrder.stream()
                .map(chunkById::get)
                .filter(Objects::nonNull)
                .map(Chunk::getText)
                .toList();
    }

    
    private JsonNode searchPinecone(List<Float> vector, UUID userId, int topK, Optional<LocalDate[]> dateRange) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("user_id", userId.toString());

        dateRange.ifPresent(range -> {
            filter.put("created_at", Map.of(
                    "$gte", range[0].toString(),
                    "$lte", range[1].toString()
            ));
        });

        Map<String, Object> body = Map.of(
            "vector", vector,
            "topK", topK,
            "includeMetadata", true,
            "filter", filter
        );

        return webClient.post()
                .uri(pineconeUrl + "/query")
                .header("Api-Key", System.getenv("PINECONE_API_KEY"))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    private List<String> extractTexts(JsonNode response) {
        List<String> texts = new ArrayList<>();
        for (JsonNode match : response.path("matches")) {
            String text = match.path("metadata").path("text").asText();
            if (!text.isEmpty()) texts.add(text);
        }
        return texts;
    }

    // private List<Float> embed(String input) {
    //     Map<String, Object> body = Map.of("input", input, "model", "text-embedding-3-small");

    //     Map response = webClient.post()
    //             .uri("https://api.openai.com/v1/embeddings")
    //             .header("Authorization", "Bearer " + apiKey)
    //             .bodyValue(body)
    //             .retrieve()
    //             .bodyToMono(Map.class)
    //             .block();

    //     List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
    //     return (List<Float>) data.get(0).get("embedding");
    // }

    // private JsonNode searchPinecone(List<Float> vector, UUID userId, int topK) {
    //     Map<String, Object> query = Map.of(
    //             "vector", vector,
    //             "topK", topK,
    //             "includeMetadata", true,
    //             "filter", Map.of("user_id", userId.toString())
    //     );

    //     return webClient.post()
    //             .uri(pineconeUrl + "/query")
    //             .header("Api-Key", System.getenv("PINECONE_API_KEY"))
    //             .bodyValue(query)
    //             .retrieve()
    //             .bodyToMono(JsonNode.class)
    //             .block();
    // }
}