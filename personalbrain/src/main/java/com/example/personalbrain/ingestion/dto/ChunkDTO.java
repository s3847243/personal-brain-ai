package com.example.personalbrain.ingestion.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.personalbrain.ingestion.model.Chunk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChunkDTO {
    private UUID id;
    private UUID documentId;
    private UUID userId;
    private String text;
    private LocalDateTime createdAt;
    private String tags;

    public static ChunkDTO fromEntity(Chunk chunk) {
        return ChunkDTO.builder()
                .id(chunk.getId())
                .documentId(chunk.getDocumentId())
                .userId(chunk.getUserId())
                .text(chunk.getText())
                .createdAt(chunk.getCreatedAt())
                .tags(chunk.getTags())
                .build();
    }
}