package com.example.personalbrain.timeline.dto;

import java.util.UUID;

import com.example.personalbrain.ingestion.model.Chunk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChunkPreviewDTO {
    private UUID id;
    private String preview;
    private String tags;

    public static ChunkPreviewDTO fromEntity(Chunk chunk) {
        return ChunkPreviewDTO.builder()
                .id(chunk.getId())
                .preview(chunk.getText().substring(0, Math.min(150, chunk.getText().length())))
                .tags(chunk.getTags())
                .build();
    }
}
