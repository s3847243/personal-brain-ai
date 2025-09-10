package com.example.personalbrain.backlinks.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "backlinks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Backlink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID chunkId;

    @Column(nullable = false)
    private UUID relatedChunkId;

    @Column(nullable = false)
    private float similarity;
}
