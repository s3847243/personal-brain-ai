package com.example.personalbrain.ingestion.service;
import com.drew.lang.annotations.Nullable;
import com.example.personalbrain.ingestion.dto.VectorData;
import com.example.personalbrain.query.service.DateRangeExtractor.EpochRange;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Struct;

import io.pinecone.clients.Index;
import io.pinecone.proto.DeleteRequest;
import io.pinecone.proto.DeleteResponse;
import io.pinecone.proto.DescribeIndexStatsRequest;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.proto.QueryRequest;
import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.ScoredVector;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.proto.Vector;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
@Service
@RequiredArgsConstructor
@Slf4j
public class PineconeService {


    private final Index pineconeIndex;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int BATCH_SIZE = 1000; // adjust if needed

        public void upsertVectors(UUID chunkId, UUID userId, UUID docId, List<Float> embedding, 
                             LocalDateTime createdAt, String tags) {
        try {
            // Create metadata
            var instant = createdAt.atOffset(java.time.ZoneOffset.UTC).toInstant(); // normalize to UTC
            var isoUtc = java.time.format.DateTimeFormatter.ISO_INSTANT.format(instant); // e.g., 2025-08-09T04:03:22.123Z
            long epochSeconds = instant.getEpochSecond();         // or instant.toEpochMilli()
            Map<String, com.google.protobuf.Value> metadataFields = new HashMap<>();
            metadataFields.put("user_id", com.google.protobuf.Value.newBuilder().setStringValue(userId.toString()).build());
            metadataFields.put("document_id", com.google.protobuf.Value.newBuilder().setStringValue(docId.toString()).build());
            // metadataFields.put("created_at", com.google.protobuf.Value.newBuilder().setStringValue(createdAt.toString()).build());
            metadataFields.put("created_at",
                                com.google.protobuf.Value.newBuilder().setStringValue(isoUtc).build());
            metadataFields.put("created_at_ts", com.google.protobuf.Value.newBuilder().setNumberValue((double) epochSeconds).build()); // <- numeric!
                    
            if (tags != null && !tags.trim().isEmpty()) {
                metadataFields.put("tags", com.google.protobuf.Value.newBuilder().setStringValue(tags).build());
            }

            Struct metadata = Struct.newBuilder().putAllFields(metadataFields).build();
            // Create VectorWithUnsignedIndices manually (no builder pattern)
            // Create VectorWithUnsignedIndices with proper constructor
            VectorWithUnsignedIndices vector = new VectorWithUnsignedIndices(
                chunkId.toString(),  // id
                embedding,           // values
                metadata, 
                null                   // sparseValues (optional, can be null)  
            );
            
            // Upsert the vector (namespace is optional, use "" for default)
            UpsertResponse response = pineconeIndex.upsert(Arrays.asList(vector), "");
            
            log.info("Successfully upserted vector with ID: {} (upserted count: {})", 
                    chunkId, response.getUpsertedCount());
                    
        } catch (Exception e) {
            log.error("Failed to upsert vector with ID: {}", chunkId, e);
            throw new RuntimeException("Failed to upsert vector to Pinecone", e);
        }
    }



    private static com.google.protobuf.Value num(double d) {
        return com.google.protobuf.Value.newBuilder().setNumberValue(d).build();
    }
    private static com.google.protobuf.Value str(String s) {
        return com.google.protobuf.Value.newBuilder().setStringValue(s).build();
    }
    private static com.google.protobuf.Value obj(java.util.Map<String, com.google.protobuf.Value> fields) {
        return com.google.protobuf.Value.newBuilder()
            .setStructValue(com.google.protobuf.Struct.newBuilder().putAllFields(fields).build())
            .build();
    }
   
    private com.google.protobuf.Struct buildFilter(UUID userId, Optional<EpochRange> epochRangeOpt) {
        var filter = com.google.protobuf.Struct.newBuilder()
            .putFields("user_id", str(userId.toString()));

        epochRangeOpt.ifPresent(r -> {
            // numeric comparison on created_at_ts
            var range = new java.util.HashMap<String, com.google.protobuf.Value>();
            range.put("$gte", num(r.startEpoch()));     // inclusive
            range.put("$lt",  num(r.endEpoch()));   // exclusive upper bound

            filter.putFields("created_at_ts", obj(range));
        });

        return filter.build();
        }

 
    public List<ScoredVectorWithUnsignedIndices> queryVectors(
        List<Float> queryEmbedding,
        int topK,
        UUID userId,
        Optional<EpochRange> epochRangeOpt

        ) {
            // helpful diagnostics
            log.info("Querying Pinecone: topK={}, dim={}, userId={}, dateRangePresent={}",
                    topK, queryEmbedding != null ? queryEmbedding.size() : -1, userId, epochRangeOpt.isPresent());

            final String namespace = ""; // ✅ use the SAME namespace you used for upsert

            try {
                Struct filter = buildFilter(userId, epochRangeOpt);
                log.info("Pinecone filter: {}", filter);

                // QueryResponseWithUnsignedIndices resp = pineconeIndex.query(
                //     topK,
                //     queryEmbedding,
                //     null,    // sparseIndices
                //     null,    // sparseValues
                //     null,    // id
                //     namespace, // ✅ empty string, not null
                //     // filter,
                //     null,
                //     true,    // includeValues
                //     true     // includeMetadata
                // );
                // Wrap the blocking call with timeout
                        CompletableFuture<QueryResponseWithUnsignedIndices> queryFuture = 
                            CompletableFuture.supplyAsync(() -> {
                                log.info("About to execute Pinecone query...");
                                return pineconeIndex.query(
                                    topK,
                                    queryEmbedding,
                                    null,    // sparseIndices
                                    null,    // sparseValues
                                    null,    // id
                                    namespace,
                                    filter,
                                    true,    // includeValues
                                    true     // includeMetadata
                                );
                            });

                        QueryResponseWithUnsignedIndices resp = queryFuture.get(30, TimeUnit.SECONDS);
                log.info("Pinecone query successful: {} matches", 
                        resp != null ? resp.getMatchesList().size() : -1);

                return resp.getMatchesList();

            } catch (io.grpc.StatusRuntimeException e) {
                // surface gRPC status—super helpful
                log.error("Pinecone query failed: status={}, desc={}", e.getStatus().getCode(), e.getStatus().getDescription(), e);
                throw new RuntimeException("Pinecone query failed: " + e.getStatus(), e);
            } catch (Exception e) {
                log.error("Pinecone query failed (unexpected)", e);
                throw new RuntimeException("Pinecone query failed", e);
            }
        }

    /**
     * Query vectors by similarity
     */
    public List<ScoredVectorWithUnsignedIndices> queryVectors(List<Float> queryEmbedding, int topK, 
                                         Map<String, String> metadataFilter) {
        try {
            QueryRequest.Builder queryBuilder = QueryRequest.newBuilder()
                .addAllVector(queryEmbedding)
                .setTopK(topK)
                .setIncludeMetadata(true)
                .setIncludeValues(true);
            Struct filterStruct = null;
            // Add metadata filter if provided
            if (metadataFilter != null && !metadataFilter.isEmpty()) {
                filterStruct = Struct.newBuilder()
                    .putAllFields(metadataFilter.entrySet().stream()
                        .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> com.google.protobuf.Value.newBuilder().setStringValue(e.getValue()).build()
                        )))
                    .build();
                queryBuilder.setFilter(filterStruct);
            }


            // QueryResponse response = pineconeIndex.query(queryBuilder.build());
            // return response.getMatchesList();
            // Call with individual parameters
            QueryResponseWithUnsignedIndices response = pineconeIndex.query(
                topK,           // topK
                queryEmbedding, // vector
                null,           // sparseIndices
                null,           // sparseValues
                null,           // id
                null,           // namespace (use null for default)
                filterStruct,   // filter
                true,           // includeValues
                true            // includeMetadata
            );
            
            return response.getMatchesList();
            
        } catch (Exception e) {
            log.error("Failed to query vectors", e);
            throw new RuntimeException("Failed to query vectors from Pinecone", e);
        }
    }
    
    /** Delete many vectors by String IDs (optionally in a namespace). */
    public DeleteResponse deleteVectors(List<String> vectorIds, String namespace) {
        if (vectorIds == null || vectorIds.isEmpty()) {
            log.info("deleteVectors called with empty id list; nothing to delete.");
            return DeleteResponse.newBuilder().build();
        }
        try {
            DeleteResponse lastResp = null;
            for (int i = 0; i < vectorIds.size(); i += BATCH_SIZE) {
                List<String> batch = vectorIds.subList(i, Math.min(i + BATCH_SIZE, vectorIds.size()));
                // deleteAll=false (we're deleting specific IDs), filter=null
                lastResp = pineconeIndex.delete(batch, false, namespace, (Struct) null);
                log.info("Deleted {} vectors in this batch (namespace: {})", batch.size(), namespace);
            }
            log.info("Successfully deleted {} vectors total", vectorIds.size());
            return lastResp == null ? DeleteResponse.newBuilder().build() : lastResp;
        } catch (Exception e) {
            log.error("Failed to delete vectors (count={}): {}", 
                      vectorIds.size(), vectorIds, e);
            throw new RuntimeException("Failed to delete vectors from Pinecone", e);
        }
    }

    /** Overload: delete many vectors by UUID IDs. */
    public DeleteResponse deleteVectorsByUuid(List<UUID> vectorIds, String namespace) {
        List<String> asStrings = vectorIds.stream().map(UUID::toString).toList();
        return deleteVectors(asStrings, namespace);
    }
    /**
     * Delete a single vector by ID
     */
        public DeleteResponse deleteVector(UUID vectorId, String namespace) {
        try {
            DeleteResponse resp = pineconeIndex.delete(
                List.of(vectorId.toString()),
                false,
                namespace,
                (Struct) null
            );
            log.info("Successfully deleted vector: {} (namespace: {})", vectorId, namespace);
            return resp;
        } catch (Exception e) {
            log.error("Failed to delete vector: {}", vectorId, e);
            throw new RuntimeException("Failed to delete vector from Pinecone", e);
        }
    }
    
   
}

