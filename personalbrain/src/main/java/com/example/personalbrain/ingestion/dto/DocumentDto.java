package com.example.personalbrain.ingestion.dto;

import java.util.UUID;

public record DocumentDto(
    UUID id,
    UUID userId,
    String originalFilename,
    String storageKey,
    String sourceType,
    String createdAt,
    String publicUrl
) {}
