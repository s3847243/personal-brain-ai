package com.example.personalbrain.ingestion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.personalbrain.ingestion.model.Chunk;
import com.example.personalbrain.ingestion.repository.ChunkRepository;

import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Document;

@Service
@RequiredArgsConstructor
public class ChunkingService {

    private final ChunkRepository chunkRepo;

    private static final int CHUNK_SIZE = 400;
    private static final int CHUNK_OVERLAP = 50;

    // Use OpenAI tokenizer to match real token count
    private final OpenAiTokenizer tokenizer = new OpenAiTokenizer("gpt-3.5-turbo");

    @Transactional
    public List<Chunk> chunkAndSave(UUID userId, UUID documentId, String fullText) {
       
            Document document = Document.from(fullText);
    
            // Create a token-based document splitter
            DocumentSplitter splitter = DocumentSplitters.recursive(CHUNK_SIZE, CHUNK_OVERLAP);
            
            // Split the document into text segments
            List<TextSegment> segments = splitter.split(document);

            List<Chunk> chunks = new ArrayList<>();
            for (TextSegment segment : segments) {
                chunks.add(Chunk.builder()
                        .userId(userId)
                        .createdAt(LocalDateTime.now())
                        .documentId(documentId)
                        .text(segment.text()) // Get the actual text from the segment
                        .build());
            }

            return chunkRepo.saveAll(chunks);
    }
}