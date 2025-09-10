package com.example.personalbrain.ingestion.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorData {
    private UUID chunkId;
    private UUID userId;
    private UUID docId;
    private List<Float> embedding;
    private LocalDateTime createdAt;
    private String tags;
}