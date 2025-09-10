package com.example.personalbrain.ingestion.worker;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.personalbrain.backlinks.service.BacklinkService;
import com.example.personalbrain.ingestion.queue.dto.EmbeddingJob;
import com.example.personalbrain.ingestion.service.OpenAiEmbeddingService;
import com.example.personalbrain.ingestion.service.PineconeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingWorker {

    private final OpenAiEmbeddingService openAi;
    private final PineconeService pinecone;
    private final BacklinkService backlinkService;

    @RabbitListener(queues = "embedding.queue")
    public void handle(EmbeddingJob job) {

        log.info("🔵 Embedding chunk {}...", job.chunkId());
        try {
            List<Float> embeddingFloats = openAi.getEmbedding(job.text());
            pinecone.upsertVectors(job.chunkId(), job.userId(), job.documentId(), embeddingFloats, job.createdAt(), job.tags());
            backlinkService.findAndSaveBacklinks(job.chunkId(), job.userId(), embeddingFloats);
            log.info("✅ Embedded and upserted chunk {}", job.chunkId());
        } catch (Exception e) {
            log.error("❌ Failed to embed chunk {}", job.chunkId(), e);
        }
    }
}