package com.example.personalbrain.ingestion.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.personalbrain.backlinks.model.Backlink;
import com.example.personalbrain.backlinks.repository.BacklinkRepository;
import com.example.personalbrain.ingestion.dto.BacklinkedChunkDTO;
import com.example.personalbrain.ingestion.dto.DocumentDto;
import com.example.personalbrain.ingestion.model.Chunk;
import com.example.personalbrain.ingestion.model.Document;
import com.example.personalbrain.ingestion.repository.ChunkRepository;
import com.example.personalbrain.ingestion.repository.DocumentRepository;
import com.example.personalbrain.ingestion.service.PineconeService;
import com.example.personalbrain.ingestion.service.S3Storage;
import com.example.personalbrain.user.model.UserPrincipal;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentRepository docRepo;
    private final ChunkRepository chunkRepo;
    private final BacklinkRepository backlinkRepo;
    private final S3Storage storage;
    private final PineconeService pineconeService;
    @Value("${aws.s3.bucket}")          private String bucket;

    private String buildPublicUrl(String key) {
        return "https://" + bucket + ".s3.amazonaws.com/" + key;
    }
    @GetMapping
    public List<DocumentDto> getUserDocuments(@AuthenticationPrincipal UserPrincipal user) {
        UUID userId = UUID.fromString(user.getUser().getId().toString());

        return docRepo.findByUserId(userId)
                .stream()
                .map(doc -> new DocumentDto(
                    doc.getId(),
                    doc.getUserId(),
                    doc.getOriginalFilename(),
                    doc.getStorageKey(),
                    doc.getSourceType(),
                    doc.getCreatedAt() != null ? doc.getCreatedAt().toString() : null,
                    buildPublicUrl(doc.getStorageKey())
                ))
                .toList();
    }

    @DeleteMapping("/{documentId}")
    @Transactional
    public void deleteDocument(
            @PathVariable UUID documentId,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        UUID userId = UUID.fromString(user.getUser().getId().toString());

        Document doc = docRepo.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (!doc.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this document");
        }

        // Delete file from storage (e.g., S3)
        if (doc.getStorageKey() != null) {
            storage.delete(doc.getStorageKey());
        }
        // 2) Collect chunk IDs once (no N+1)
        List<UUID> chunkIds = chunkRepo.findIdsByDocumentId(documentId);
        // Delete all chunks
        // for (var chunk : chunkIds) {
        //     pineconeService.deleteVector(chunk.getId());
        //     chunkRepo.delete(chunk);
        // }
        if (!chunkIds.isEmpty()) {
            // 3) Delete backlinks that reference any of these chunks
            backlinkRepo.deleteAllByAnyChunkIdIn(chunkIds);

            // 4) Delete vectors in Pinecone (ideally batch API)
            try {
                pineconeService.deleteVectorsByUuid(chunkIds,null); // implement batch; else loop
            } catch (Exception e) {
                // log and proceed (DB txn will still commit)
            }

            // 5) Delete chunks in bulk
            chunkRepo.deleteByDocumentId(documentId);
        }
        // Finally delete the document itself
        docRepo.delete(doc);
    }
    @GetMapping("/{docId}/related")
    public List<BacklinkedChunkDTO> getBacklinksForDocument(@PathVariable UUID docId) {
        // 1. Get all chunks in this document
        List<Chunk> docChunks = chunkRepo.findByDocumentId(docId);
        if (docChunks.isEmpty()) return List.of();
        List<UUID> chunkIds = docChunks.stream().map(Chunk::getId).toList();

        // 2. Get all backlinks from these chunks
        List<Backlink> backlinks = backlinkRepo.findByChunkIdIn(chunkIds);
        if (backlinks.isEmpty()) return List.of();
        // 3. Get related chunk info
        List<UUID> relatedIds = backlinks.stream().map(Backlink::getRelatedChunkId).distinct().toList();
        List<Chunk> relatedChunks = chunkRepo.findAllById(relatedIds);

        // 4. Map to a DTO
        Map<UUID, Chunk> relatedMap = relatedChunks.stream()
            .collect(Collectors.toMap(Chunk::getId, Function.identity()));
        // 4) fetch related documents (by the related chunks’ documentId)
        List<UUID> relatedDocIds = relatedChunks.stream()
            .map(Chunk::getDocumentId)       
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<UUID, Document> relatedDocMap = docRepo.findAllById(relatedDocIds).stream()
        .collect(Collectors.toMap(Document::getId, Function.identity()));
        return backlinks.stream()
        .map(bl -> {
            Chunk rc = relatedMap.get(bl.getRelatedChunkId());
            UUID relDocId = rc != null ? rc.getDocumentId() : null;
            Document relDoc = relDocId != null ? relatedDocMap.get(relDocId) : null;
            return new BacklinkedChunkDTO(
                bl.getChunkId(),
                bl.getRelatedChunkId(),
                rc != null ? rc.getText() : null,
                relDoc != null ? relDoc.getId() : null,
                relDoc != null ? relDoc.getOriginalFilename() : null    
            );
        })
        .toList();
    }
}
