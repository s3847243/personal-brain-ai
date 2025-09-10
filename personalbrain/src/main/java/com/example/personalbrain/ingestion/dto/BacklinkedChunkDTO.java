package com.example.personalbrain.ingestion.dto;

import java.util.UUID;

public record BacklinkedChunkDTO(
    UUID sourceChunkId,
    UUID relatedChunkId,
    String relatedText,
    UUID relatedDocumentId,
    String relatedDocumentTitle
) {}
