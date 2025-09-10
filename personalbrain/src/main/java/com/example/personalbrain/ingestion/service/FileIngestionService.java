package com.example.personalbrain.ingestion.service;

import java.security.Principal;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.personalbrain.ingestion.model.Document;
import com.example.personalbrain.ingestion.model.ObjectStorage;
import com.example.personalbrain.ingestion.queue.dto.IngestionJob;
import com.example.personalbrain.ingestion.queue.producer.JobPublisher;
import com.example.personalbrain.ingestion.repository.DocumentRepository;
import com.example.personalbrain.user.model.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileIngestionService {
    private final ObjectStorage storage;
    private final DocumentRepository docRepo;
    private final JobPublisher publisher;

    public Document handleUpload(MultipartFile file, @AuthenticationPrincipal UserPrincipal user) {
        try {
            UUID userId = UUID.fromString(user.getUser().getId().toString());
            String key = storage.put(
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType(),
                    file.getOriginalFilename()
            );

            Document doc = Document.builder()
                    .userId(userId)
                    .originalFilename(file.getOriginalFilename())
                    .storageKey(key)
                    .sourceType("FILE")
                    .build();

            docRepo.save(doc);
            publisher.publish(new IngestionJob(doc.getId(), userId, key));
            return doc;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload and queue ingestion job", e);
        }
    }  
}
