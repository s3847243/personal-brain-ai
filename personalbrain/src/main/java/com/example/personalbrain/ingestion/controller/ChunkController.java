package com.example.personalbrain.ingestion.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.personalbrain.backlinks.model.Backlink;
import com.example.personalbrain.backlinks.repository.BacklinkRepository;
import com.example.personalbrain.ingestion.dto.ChunkDTO;
import com.example.personalbrain.ingestion.repository.ChunkRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chunks")
@RequiredArgsConstructor
public class ChunkController {
    private final ChunkRepository chunkRepo;
    private final BacklinkRepository backlinkRepo;
    @GetMapping("/{id}/related")
    public List<ChunkDTO> getRelatedChunks(@PathVariable UUID id) {
            var backlinks = backlinkRepo.findByChunkId(id);
            var relatedIds = backlinks.stream().map(Backlink::getRelatedChunkId).toList();
                return chunkRepo.findAllById(relatedIds)
            .stream()
            .map(ChunkDTO::fromEntity)
            .toList();
    }
}
