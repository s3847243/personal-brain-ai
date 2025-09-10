package com.example.personalbrain.ingestion.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documents")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Document {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private UUID userId;
    private String originalFilename;
    @Column(nullable = false)
    private String storageKey;     // s3://bucket/key 
    @Column(nullable = false)
    private String sourceType;     // FILE | URL
    private LocalDateTime createdAt;


}