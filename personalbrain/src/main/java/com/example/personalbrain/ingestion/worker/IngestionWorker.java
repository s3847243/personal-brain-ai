package com.example.personalbrain.ingestion.worker;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.personalbrain.ingestion.model.Chunk;
import com.example.personalbrain.ingestion.model.Document;
import com.example.personalbrain.ingestion.model.ObjectStorage;
import com.example.personalbrain.ingestion.queue.dto.EmbeddingJob;
import com.example.personalbrain.ingestion.queue.dto.IngestionJob;
import com.example.personalbrain.ingestion.queue.producer.EmbeddingPublisher;
import com.example.personalbrain.ingestion.repository.DocumentRepository;
import com.example.personalbrain.ingestion.service.ChunkingService;
import com.example.personalbrain.ingestion.service.S3Storage;
import com.example.personalbrain.ingestion.service.TextExtractor;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = "ingest.queue")
class IngestionWorker {
    private final TextExtractor extractor;  // uses Tika & Jsoup
    //private final ChunkProducer chunkProducer; // will chunk+embed later
    private final ChunkingService chunker;
    private final DocumentRepository docRepo;
    private final ObjectStorage storage;
    private final S3Storage s3Storage; // if needed
    private final EmbeddingPublisher embeddingPublisher;
    @RabbitListener(queues = "ingest.queue")
    @Transactional
    public void handleJob(IngestionJob job) {
        log.info("⏳ IngestionWorker started for doc {}", job.documentId());
        Document doc = docRepo.findById(job.documentId())
                .orElseThrow(() -> new IllegalStateException("Document not found"));

 
        try (InputStream originalStream = s3Storage.get(job.storageKey())) {
        byte[] fileBytes = originalStream.readAllBytes();
        log.info("📊 Downloaded {} bytes from S3", fileBytes.length);
        
        if (fileBytes.length == 0) {
            log.error("❌ S3 object is empty!");
            return;
        }
        
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(fileBytes)) {
            String rawText = extractor.extract(byteStream);
            log.info("✅ Extracted text for doc {}: {} chars", doc.getId(), rawText.length());
            
            List<Chunk> chunks = chunker.chunkAndSave(doc.getUserId(), doc.getId(), rawText);
            log.info("✅ Saved {} chunks for doc {}", chunks.size(), doc.getId());

            for (Chunk c : chunks) {
                embeddingPublisher.publish(new EmbeddingJob(
                    c.getId(), c.getUserId(), c.getDocumentId(), c.getText(),
                    c.getCreatedAt(), c.getTags()
                ));
            }
        }
        } catch (Exception e) {
            log.error("❌ Ingestion failed for doc {}", job.documentId(), e);
        }
    }
}
