// package com.example.personalbrain.timeline.service;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.JsonNode;
// import com.theokanning.openai.completion.chat.ChatCompletionRequest;
// import com.theokanning.openai.completion.chat.ChatMessage;
// import com.theokanning.openai.completion.chat.ChatMessageRole;
// import com.theokanning.openai.embedding.EmbeddingRequest;
// import com.theokanning.openai.service.OpenAiService;
// import io.pinecone.PineconeClient;
// import io.pinecone.PineconeConnection;
// import org.springframework.stereotype.Service;

// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.*;
// import java.util.stream.Collectors;

// @Service
// @RequiredArgsConstructor
// public class SemanticTimelineService {
    
//     private final OpenAiService openAiService;
//     private final PineconeClient pineconeClient;
//     private final PineconeConnection pineconeConnection;
//     private final ObjectMapper objectMapper;
//     private final String indexName;
    

    
//     /**
//      * Main method to generate semantic timeline for a single document
//      */
//     public List<TimelineEvent> generateSemanticTimeline(String documentId, String userId) {
//         return generateSemanticTimeline(List.of(documentId), userId);
//     }
    
//     /**
//      * Generate semantic timeline across multiple documents
//      */
//     public List<TimelineEvent> generateSemanticTimeline(List<String> documentIds, String userId) {
//         try {
//             // 1. Query Pinecone for all chunks related to these documents
//             List<DocumentChunk> chunks = getDocumentChunks(documentIds, userId);
            
//             if (chunks.isEmpty()) {
//                 return new ArrayList<>();
//             }
            
//             // 2. Extract temporal information from chunks
//             List<TemporalInfo> temporalInfos = extractTemporalInformation(chunks);
            
//             // 3. Create cross-document semantic clusters
//             List<SemanticCluster> clusters = createCrossDocumentSemanticClusters(chunks, 0.75);
            
//             // 4. Generate timeline events from clusters and temporal info
//             List<TimelineEvent> events = generateTimelineEvents(clusters, temporalInfos, chunks);
            
//             // 5. Merge related events and resolve conflicts
//             List<TimelineEvent> mergedEvents = mergeRelatedEvents(events);
            
//             // 6. Sort events chronologically
//             return sortEventsByTime(mergedEvents);
            
//         } catch (Exception e) {
//             throw new RuntimeException("Error generating semantic timeline", e);
//         }
//     }
    
//     /**
//      * Generate timeline for all documents belonging to a user
//      */
//     public List<TimelineEvent> generateUserTimeline(String userId) {
//         try {
//             // Get all documents for user
//             List<DocumentChunk> chunks = getAllUserDocumentChunks(userId);
            
//             if (chunks.isEmpty()) {
//                 return new ArrayList<>();
//             }
            
//             // Extract temporal information
//             List<TemporalInfo> temporalInfos = extractTemporalInformation(chunks);
            
//             // Create cross-document semantic clusters
//             List<SemanticCluster> clusters = createCrossDocumentSemanticClusters(chunks, 0.70);
            
//             // Generate and merge timeline events
//             List<TimelineEvent> events = generateTimelineEvents(clusters, temporalInfos, chunks);
//             List<TimelineEvent> mergedEvents = mergeRelatedEvents(events);
            
//             return sortEventsByTime(mergedEvents);
            
//         } catch (Exception e) {
//             throw new RuntimeException("Error generating user timeline", e);
//         }
//     }
    
//     /**
//      * Query Pinecone to get all chunks for multiple documents
//      */
//     private List<DocumentChunk> getDocumentChunks(List<String> documentIds, String userId) {
//         try {
//             List<Float> dummyVector = Collections.nCopies(1536, 0.0f);
            
//             Map<String, Object> filter = Map.of(
//                 "documentId", Map.of("$in", documentIds),
//                 "userId", userId
//             );
            
//             var queryRequest = PineconeConnection.QueryRequest.builder()
//                     .vector(dummyVector)
//                     .filter(filter)
//                     .topK(10000) // Increased for multiple documents
//                     .includeMetadata(true)
//                     .includeValues(true)
//                     .build();
            
//             var queryResponse = pineconeConnection.query(queryRequest);
            
//             return queryResponse.getMatches().stream()
//                     .map(match -> DocumentChunk.builder()
//                             .id(match.getId())
//                             .text((String) match.getMetadata().get("text"))
//                             .chunkIndex((Integer) match.getMetadata().get("chunkIndex"))
//                             .documentId((String) match.getMetadata().get("documentId"))
//                             .documentTitle((String) match.getMetadata().get("documentTitle"))
//                             .embedding(match.getValues())
//                             .uploadTimestamp((String) match.getMetadata().get("uploadTimestamp"))
//                             .build())
//                     .collect(Collectors.toList());
                    
//         } catch (Exception e) {
//             throw new RuntimeException("Error querying document chunks", e);
//         }
//     }
    
//     /**
//      * Get all document chunks for a user (across all documents)
//      */
//     private List<DocumentChunk> getAllUserDocumentChunks(String userId) {
//         try {
//             List<Float> dummyVector = Collections.nCopies(1536, 0.0f);
            
//             Map<String, Object> filter = Map.of("userId", userId);
            
//             var queryRequest = PineconeConnection.QueryRequest.builder()
//                     .vector(dummyVector)
//                     .filter(filter)
//                     .topK(10000)
//                     .includeMetadata(true)
//                     .includeValues(true)
//                     .build();
            
//             var queryResponse = pineconeConnection.query(queryRequest);
            
//             return queryResponse.getMatches().stream()
//                     .map(match -> DocumentChunk.builder()
//                             .id(match.getId())
//                             .text((String) match.getMetadata().get("text"))
//                             .chunkIndex((Integer) match.getMetadata().get("chunkIndex"))
//                             .documentId((String) match.getMetadata().get("documentId"))
//                             .documentTitle((String) match.getMetadata().get("documentTitle"))
//                             .embedding(match.getValues())
//                             .uploadTimestamp((String) match.getMetadata().get("uploadTimestamp"))
//                             .build())
//                     .collect(Collectors.toList());
                    
//         } catch (Exception e) {
//             throw new RuntimeException("Error querying user document chunks", e);
//         }
//     }
    
//     /**
//      * Extract temporal information using OpenAI
//      */
//     private List<TemporalInfo> extractTemporalInformation(List<DocumentChunk> chunks) {
//         try {
//             StringBuilder prompt = new StringBuilder();
//             prompt.append("Extract all dates, time periods, and temporal references from the following text chunks. ");
//             prompt.append("Return a JSON array with objects containing:\n");
//             prompt.append("- chunkId: the chunk identifier\n");
//             prompt.append("- temporalReferences: array of temporal mentions\n");
//             prompt.append("- estimatedDate: best guess at the main date/period (ISO format if possible)\n");
//             prompt.append("- confidence: confidence level (high/medium/low)\n\n");
//             prompt.append("Text chunks:\n");
            
//             for (int i = 0; i < chunks.size(); i++) {
//                 prompt.append("Chunk ").append(chunks.get(i).getId()).append(": ")
//                       .append(chunks.get(i).getText()).append("\n\n");
//             }
            
//             ChatMessage message = new ChatMessage(ChatMessageRole.USER.value(), prompt.toString());
            
//             ChatCompletionRequest request = ChatCompletionRequest.builder()
//                     .model("gpt-4")
//                     .messages(List.of(message))
//                     .temperature(0.1)
//                     .build();
            
//             var response = openAiService.createChatCompletion(request);
//             String jsonResponse = response.getChoices().get(0).getMessage().getContent();
            
//             JsonNode jsonNode = objectMapper.readTree(jsonResponse);
//             List<TemporalInfo> temporalInfos = new ArrayList<>();
            
//             for (JsonNode node : jsonNode) {
//                 TemporalInfo info = TemporalInfo.builder()
//                         .chunkId(node.get("chunkId").asText())
//                         .temporalReferences(extractStringArray(node.get("temporalReferences")))
//                         .estimatedDate(node.get("estimatedDate").asText())
//                         .confidence(node.get("confidence").asText())
//                         .build();
//                 temporalInfos.add(info);
//             }
            
//             return temporalInfos;
            
//         } catch (Exception e) {
//             // Fallback: return empty temporal info for all chunks
//             return chunks.stream()
//                     .map(chunk -> TemporalInfo.builder()
//                             .chunkId(chunk.getId())
//                             .temporalReferences(new ArrayList<>())
//                             .estimatedDate(null)
//                             .confidence("low")
//                             .build())
//                     .collect(Collectors.toList());
//         }
//     }
    
//     /**
//      * Create cross-document semantic clusters that can span multiple documents
//      */
//     private List<SemanticCluster> createCrossDocumentSemanticClusters(List<DocumentChunk> chunks, double threshold) {
//         List<SemanticCluster> clusters = new ArrayList<>();
//         Set<String> processed = new HashSet<>();
        
//         // Group chunks by document first for better organization
//         Map<String, List<DocumentChunk>> chunksByDocument = chunks.stream()
//                 .collect(Collectors.groupBy(DocumentChunk::getDocumentId));
        
//         for (DocumentChunk chunk : chunks) {
//             if (processed.contains(chunk.getId())) {
//                 continue;
//             }
            
//             SemanticCluster cluster = SemanticCluster.builder()
//                     .id("cluster_" + clusters.size())
//                     .centerChunk(chunk)
//                     .members(new ArrayList<>(List.of(chunk)))
//                     .topics(new ArrayList<>())
//                     .documentIds(new HashSet<>(Set.of(chunk.getDocumentId())))
//                     .crossDocument(false)
//                     .build();
            
//             // Find similar chunks across ALL documents
//             for (DocumentChunk otherChunk : chunks) {
//                 if (processed.contains(otherChunk.getId()) || 
//                     chunk.getId().equals(otherChunk.getId())) {
//                     continue;
//                 }
                
//                 double similarity = calculateCosineSimilarity(chunk.getEmbedding(), otherChunk.getEmbedding());
//                 if (similarity >= threshold) {
//                     cluster.getMembers().add(otherChunk);
//                     cluster.getDocumentIds().add(otherChunk.getDocumentId());
//                     processed.add(otherChunk.getId());
                    
//                     // Mark as cross-document if chunks from different documents
//                     if (!otherChunk.getDocumentId().equals(chunk.getDocumentId())) {
//                         cluster.setCrossDocument(true);
//                     }
//                 }
//             }
            
//             clusters.add(cluster);
//             processed.add(chunk.getId());
//         }
        
//         return clusters;
//     }
    
//     /**
//      * Merge related timeline events that might be duplicates or very similar
//      */
//     private List<TimelineEvent> mergeRelatedEvents(List<TimelineEvent> events) {
//         List<TimelineEvent> mergedEvents = new ArrayList<>();
//         Set<String> processed = new HashSet<>();
        
//         for (TimelineEvent event : events) {
//             if (processed.contains(event.getId())) {
//                 continue;
//             }
            
//             // Find events that should be merged with this one
//             List<TimelineEvent> toMerge = events.stream()
//                     .filter(other -> !processed.contains(other.getId()))
//                     .filter(other -> shouldMergeEvents(event, other))
//                     .collect(Collectors.toList());
            
//             if (toMerge.size() > 1) {
//                 // Merge multiple events
//                 TimelineEvent mergedEvent = mergeMultipleEvents(toMerge);
//                 mergedEvents.add(mergedEvent);
//                 toMerge.forEach(e -> processed.add(e.getId()));
//             } else {
//                 // Keep the original event
//                 mergedEvents.add(event);
//                 processed.add(event.getId());
//             }
//         }
        
//         return mergedEvents;
//     }
    
//     /**
//      * Determine if two events should be merged
//      */
//     private boolean shouldMergeEvents(TimelineEvent event1, TimelineEvent event2) {
//         // Check if events are very close in time (within 1 day)
//         long hoursDiff = Math.abs(
//             java.time.Duration.between(event1.getTimestamp(), event2.getTimestamp()).toHours()
//         );
        
//         if (hoursDiff > 24) {
//             return false;
//         }
        
//         // Check semantic similarity of topics
//         Set<String> topics1 = new HashSet<>(event1.getTopics());
//         Set<String> topics2 = new HashSet<>(event2.getTopics());
        
//         Set<String> intersection = new HashSet<>(topics1);
//         intersection.retainAll(topics2);
        
//         Set<String> union = new HashSet<>(topics1);
//         union.addAll(topics2);
        
//         double topicSimilarity = union.isEmpty() ? 0 : (double) intersection.size() / union.size();
        
//         return topicSimilarity > 0.6; // 60% topic overlap
//     }
    
//     /**
//      * Merge multiple similar events into one
//      */
//     private TimelineEvent mergeMultipleEvents(List<TimelineEvent> events) {
//         if (events.isEmpty()) {
//             throw new IllegalArgumentException("Cannot merge empty list of events");
//         }
        
//         if (events.size() == 1) {
//             return events.get(0);
//         }
        
//         // Use the event with highest confidence as the base
//         TimelineEvent baseEvent = events.stream()
//                 .max(Comparator.comparing(TimelineEvent::getConfidence))
//                 .orElse(events.get(0));
        
//         // Merge all chunk IDs
//         List<String> allChunkIds = events.stream()
//                 .flatMap(e -> e.getChunkIds().stream())
//                 .distinct()
//                 .collect(Collectors.toList());
        
//         // Merge all topics
//         List<String> allTopics = events.stream()
//                 .flatMap(e -> e.getTopics().stream())
//                 .distinct()
//                 .collect(Collectors.toList());
        
//         // Get all document IDs involved
//         Set<String> documentIds = events.stream()
//                 .flatMap(e -> e.getDocumentIds().stream())
//                 .collect(Collectors.toSet());
        
//         // Combine descriptions
//         String combinedDescription = events.stream()
//                 .map(TimelineEvent::getDescription)
//                 .distinct()
//                 .collect(Collectors.joining(" | "));
        
//         // Calculate average confidence
//         double avgConfidence = events.stream()
//                 .mapToDouble(TimelineEvent::getConfidence)
//                 .average()
//                 .orElse(0.5);
        
//         return TimelineEvent.builder()
//                 .id(UUID.randomUUID().toString())
//                 .title(baseEvent.getTitle() + " (merged)")
//                 .description(combinedDescription)
//                 .timestamp(baseEvent.getTimestamp())
//                 .confidence(avgConfidence)
//                 .chunkIds(allChunkIds)
//                 .semanticClusterId(baseEvent.getSemanticClusterId())
//                 .topics(allTopics)
//                 .documentIds(new ArrayList<>(documentIds))
//                 .crossDocument(documentIds.size() > 1)
//                 .mergedEventIds(events.stream().map(TimelineEvent::getId).collect(Collectors.toList()))
//                 .build();
//     }
    
//     /**
//      * Calculate cosine similarity between two vectors
//      */
//     private double calculateCosineSimilarity(List<Float> vectorA, List<Float> vectorB) {
//         if (vectorA.size() != vectorB.size()) {
//             throw new IllegalArgumentException("Vectors must have the same dimension");
//         }
        
//         double dotProduct = 0.0;
//         double normA = 0.0;
//         double normB = 0.0;
        
//         for (int i = 0; i < vectorA.size(); i++) {
//             dotProduct += vectorA.get(i) * vectorB.get(i);
//             normA += Math.pow(vectorA.get(i), 2);
//             normB += Math.pow(vectorB.get(i), 2);
//         }
        
//         if (normA == 0.0 || normB == 0.0) {
//             return 0.0;
//         }
        
//         return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
//     }
    
//     /**
//      * Generate timeline events from clusters and temporal information
//      */
//     private List<TimelineEvent> generateTimelineEvents(List<SemanticCluster> clusters, 
//                                                      List<TemporalInfo> temporalInfos,
//                                                      List<DocumentChunk> chunks) {
//         List<TimelineEvent> events = new ArrayList<>();
//         Map<String, TemporalInfo> temporalMap = temporalInfos.stream()
//                 .collect(Collectors.toMap(TemporalInfo::getChunkId, info -> info));
        
//         for (SemanticCluster cluster : clusters) {
//             // Generate topic summary for the cluster
//             String topicSummary = generateTopicSummary(cluster);
            
//             // Find the best temporal reference for this cluster
//             TemporalInfo bestTemporal = findBestTemporalInfo(cluster, temporalMap);
            
//             // Create timeline event
//             TimelineEvent event = TimelineEvent.builder()
//                     .id(UUID.randomUUID().toString())
//                     .title(generateEventTitle(cluster, bestTemporal))
//                     .description(topicSummary)
//                     .timestamp(parseTimestamp(bestTemporal))
//                     .confidence(calculateEventConfidence(cluster, bestTemporal))
//                     .chunkIds(cluster.getMembers().stream()
//                             .map(DocumentChunk::getId)
//                             .collect(Collectors.toList()))
//                     .semanticClusterId(cluster.getId())
//                     .topics(extractTopics(topicSummary))
//                     .build();
            
//             events.add(event);
//         }
        
//         return events;
//     }
    
//     /**
//      * Generate a topic summary for a semantic cluster using OpenAI
//      */
//     private String generateTopicSummary(SemanticCluster cluster) {
//         try {
//             StringBuilder prompt = new StringBuilder();
//             prompt.append("Summarize the main topic and key points from these related text chunks in 2-3 sentences:\n\n");
            
//             for (DocumentChunk chunk : cluster.getMembers()) {
//                 prompt.append("- ").append(chunk.getText()).append("\n");
//             }
            
//             ChatMessage message = new ChatMessage(ChatMessageRole.USER.value(), prompt.toString());
            
//             ChatCompletionRequest request = ChatCompletionRequest.builder()
//                     .model("gpt-3.5-turbo")
//                     .messages(List.of(message))
//                     .temperature(0.3)
//                     .maxTokens(150)
//                     .build();
            
//             var response = openAiService.createChatCompletion(request);
//             return response.getChoices().get(0).getMessage().getContent().trim();
            
//         } catch (Exception e) {
//             // Fallback: use first chunk text (truncated)
//             String firstChunkText = cluster.getCenterChunk().getText();
//             return firstChunkText.length() > 200 ? 
//                    firstChunkText.substring(0, 200) + "..." : 
//                    firstChunkText;
//         }
//     }
    
//     /**
//      * Find the best temporal information for a cluster
//      */
//     private TemporalInfo findBestTemporalInfo(SemanticCluster cluster, Map<String, TemporalInfo> temporalMap) {
//         return cluster.getMembers().stream()
//                 .map(chunk -> temporalMap.get(chunk.getId()))
//                 .filter(Objects::nonNull)
//                 .filter(temporal -> temporal.getEstimatedDate() != null && !temporal.getEstimatedDate().isEmpty())
//                 .max(Comparator.comparing(temporal -> {
//                     // Prioritize by confidence level
//                     switch (temporal.getConfidence().toLowerCase()) {
//                         case "high": return 3;
//                         case "medium": return 2;
//                         case "low": return 1;
//                         default: return 0;
//                     }
//                 }))
//                 .orElse(null);
//     }
    
//     /**
//      * Generate an appropriate title for the timeline event
//      */
//     private String generateEventTitle(SemanticCluster cluster, TemporalInfo temporal) {
//         String baseTitle = "Document Event";
        
//         if (temporal != null && !temporal.getTemporalReferences().isEmpty()) {
//             baseTitle = temporal.getTemporalReferences().get(0);
//         }
        
//         // Try to extract a meaningful title from the cluster content
//         String content = cluster.getCenterChunk().getText();
//         if (content.length() > 50) {
//             String[] sentences = content.split("\\.");
//             if (sentences.length > 0) {
//                 String firstSentence = sentences[0].trim();
//                 if (firstSentence.length() < 100) {
//                     return firstSentence;
//                 }
//             }
//         }
        
//         return baseTitle;
//     }
    
//     /**
//      * Parse timestamp from temporal information
//      */
//     private LocalDateTime parseTimestamp(TemporalInfo temporal) {
//         if (temporal == null || temporal.getEstimatedDate() == null) {
//             return LocalDateTime.now(); // Default to current time
//         }
        
//         try {
//             // Try various date formats
//             String dateStr = temporal.getEstimatedDate();
            
//             // ISO format
//             if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
//                 return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//             }
            
//             // Add more date parsing logic as needed
//             return LocalDateTime.now();
            
//         } catch (Exception e) {
//             return LocalDateTime.now();
//         }
//     }
    
//     /**
//      * Calculate confidence score for the event
//      */
//     private double calculateEventConfidence(SemanticCluster cluster, TemporalInfo temporal) {
//         double baseConfidence = 0.5;
        
//         // Boost confidence based on cluster size
//         baseConfidence += Math.min(cluster.getMembers().size() * 0.1, 0.3);
        
//         // Boost confidence based on temporal confidence
//         if (temporal != null) {
//             switch (temporal.getConfidence().toLowerCase()) {
//                 case "high": baseConfidence += 0.3; break;
//                 case "medium": baseConfidence += 0.2; break;
//                 case "low": baseConfidence += 0.1; break;
//             }
//         }
        
//         return Math.min(baseConfidence, 1.0);
//     }
    
//     /**
//      * Extract topics from summary text
//      */
//     private List<String> extractTopics(String summary) {
//         // Simple topic extraction - you can enhance this with NLP libraries
//         return Arrays.stream(summary.split("[,.]"))
//                 .map(String::trim)
//                 .filter(s -> s.length() > 5)
//                 .limit(3)
//                 .collect(Collectors.toList());
//     }
    
//     /**
//      * Sort timeline events by timestamp
//      */
//     private List<TimelineEvent> sortEventsByTime(List<TimelineEvent> events) {
//         return events.stream()
//                 .sorted(Comparator.comparing(TimelineEvent::getTimestamp))
//                 .collect(Collectors.toList());
//     }
    
//     /**
//      * Helper method to extract string array from JSON node
//      */
//     private List<String> extractStringArray(JsonNode arrayNode) {
//         List<String> result = new ArrayList<>();
//         if (arrayNode != null && arrayNode.isArray()) {
//             for (JsonNode node : arrayNode) {
//                 result.add(node.asText());
//             }
//         }
//         return result;
//     }
// }

// // Data Transfer Objects

// @lombok.Builder
// @lombok.Data
// class DocumentChunk {
//     private String id;
//     private String text;
//     private Integer chunkIndex;
//     private String documentId;
//     private String documentTitle;
//     private List<Float> embedding;
//     private String uploadTimestamp;
// }

// @lombok.Builder
// @lombok.Data
// class TemporalInfo {
//     private String chunkId;
//     private List<String> temporalReferences;
//     private String estimatedDate;
//     private String confidence;
// }

// @lombok.Builder
// @lombok.Data
// class SemanticCluster {
//     private String id;
//     private DocumentChunk centerChunk;
//     private List<DocumentChunk> members;
//     private List<String> topics;
//     private Set<String> documentIds;
//     private boolean crossDocument;
// }

// @lombok.Builder
// @lombok.Data
// class TimelineEvent {
//     private String id;
//     private String title;
//     private String description;
//     private LocalDateTime timestamp;
//     private double confidence;
//     private List<String> chunkIds;
//     private String semanticClusterId;
//     private List<String> topics;
//     private List<String> documentIds;
//     private boolean crossDocument;
//     private List<String> mergedEventIds;
// }

// // REST Controller for API endpoints

// @RestController
// @RequestMapping("/api/timeline")
// public class SemanticTimelineController {
    
//     private final SemanticTimelineService timelineService;
    
//     public SemanticTimelineController(SemanticTimelineService timelineService) {
//         this.timelineService = timelineService;
//     }
    
//     @GetMapping("/document/{documentId}")
//     public ResponseEntity<List<TimelineEvent>> getDocumentTimeline(
//             @PathVariable String documentId,
//             @RequestParam String userId) {
        
//         try {
//             List<TimelineEvent> timeline = timelineService.generateSemanticTimeline(documentId, userId);
//             return ResponseEntity.ok(timeline);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//         }
//     }
    
//     @PostMapping("/documents")
//     public ResponseEntity<List<TimelineEvent>> getMultiDocumentTimeline(
//             @RequestBody List<String> documentIds,
//             @RequestParam String userId) {
        
//         try {
//             List<TimelineEvent> timeline = timelineService.generateSemanticTimeline(documentIds, userId);
//             return ResponseEntity.ok(timeline);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//         }
//     }
    
//     @GetMapping("/user/{userId}")
//     public ResponseEntity<List<TimelineEvent>> getUserTimeline(@PathVariable String userId) {
//         try {
//             List<TimelineEvent> timeline = timelineService.generateUserTimeline(userId);
//             return ResponseEntity.ok(timeline);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//         }
//     }
    
//     @GetMapping("/events/search")
//     public ResponseEntity<List<TimelineEvent>> searchTimelineEvents(
//             @RequestParam String query,
//             @RequestParam String userId,
//             @RequestParam(required = false) List<String> documentIds) {
        
//         // Implement semantic search for timeline events
//         // This would use embeddings to find relevant events
//         return ResponseEntity.ok(new ArrayList<>());
//     }
    
//     @GetMapping("/events/cross-document")
//     public ResponseEntity<List<TimelineEvent>> getCrossDocumentEvents(
//             @RequestParam String userId,
//             @RequestParam(required = false) List<String> documentIds) {
        
//         try {
//             List<String> docs = documentIds != null ? documentIds : new ArrayList<>();
//             List<TimelineEvent> timeline = timelineService.generateSemanticTimeline(docs, userId);
            
//             // Filter only cross-document events
//             List<TimelineEvent> crossDocEvents = timeline.stream()
//                     .filter(TimelineEvent::isCrossDocument)
//                     .collect(Collectors.toList());
                    
//             return ResponseEntity.ok(crossDocEvents);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//         }
//     }
// }