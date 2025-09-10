package com.example.personalbrain.backlinks.service;
import com.example.personalbrain.backlinks.model.Backlink;
import com.example.personalbrain.backlinks.repository.BacklinkRepository;
import com.example.personalbrain.ingestion.service.PineconeService;
import com.fasterxml.jackson.databind.JsonNode;

import io.pinecone.proto.ScoredVector;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
@Service
@RequiredArgsConstructor
public class BacklinkService {

    private final WebClient webClient = WebClient.create();
    private final BacklinkRepository backlinkRepo;
    private final PineconeService pineconeService;


    public void findAndSaveBacklinks(UUID chunkId, UUID userId, List<Float> embedding) {
        Map<String, String> filter = Map.of("user_id", userId.toString());
        List<ScoredVectorWithUnsignedIndices> matches = pineconeService.queryVectors(embedding, 3, filter);

        List<Backlink> backlinks = matches.stream()
            .filter(match -> !match.getId().equals(chunkId.toString()))  // skip self
            .map(match -> Backlink.builder()
                .chunkId(chunkId)
                .relatedChunkId(UUID.fromString(match.getId()))
                .similarity(match.getScore())
                .build())
            .toList();

        backlinkRepo.saveAll(backlinks);

        
    }

    
}