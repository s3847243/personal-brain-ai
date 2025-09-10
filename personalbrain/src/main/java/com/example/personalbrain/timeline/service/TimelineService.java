// package com.example.personalbrain.timeline.service;

// import java.util.stream.Collectors;
// import java.util.stream.Stream;

// import org.springframework.stereotype.Service;

// import com.example.personalbrain.ingestion.model.Chunk;
// import com.example.personalbrain.ingestion.repository.ChunkRepository;
// import com.example.personalbrain.ingestion.service.PineconeService;
// import com.example.personalbrain.timeline.dto.ChunkPreviewDTO;
// import com.example.personalbrain.timeline.dto.TimelineGroupDTO;

// import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;

// import java.time.format.DateTimeFormatter;

// import lombok.RequiredArgsConstructor;
// import java.util.*;

// @Service
// @RequiredArgsConstructor
// public class TimelineService {

//     private final ChunkRepository chunkRepo;
//     private final PineconeService pineconeService;
//     public List<TimelineGroupDTO> buildSemanticTimeline(UUID userId) {
//         List<Chunk> allChunks = chunkRepo.findByUserId(userId);
//         Map<UUID, TimelineGroupDTO> groupMap = new LinkedHashMap<>();
//         Set<UUID> visited = new HashSet<>();

//         for (Chunk c : allChunks) {
//             if (visited.contains(c.getId())) continue;

//             Map<String, String> filter = Map.of("user_id", userId.toString());
//             List<ScoredVectorWithUnsignedIndices> matches = pinecone.queryVectors(
//                 c.getEmbedding(), 10, filter
//             );

//             List<Chunk> relatedChunks = matches.stream()
//                 .map(match -> UUID.fromString(match.getId()))
//                 .filter(id -> !id.equals(c.getId()))
//                 .distinct()
//                 .map(id -> chunkRepo.findById(id).orElse(null))
//                 .filter(Objects::nonNull)
//                 .filter(ch -> !visited.contains(ch.getId()))
//                 .toList();

//             visited.add(c.getId());
//             relatedChunks.forEach(chunk -> visited.add(chunk.getId()));

//             List<ChunkPreviewDTO> previews = Stream.concat(
//                     Stream.of(c),
//                     relatedChunks.stream()
//             ).map(ChunkPreviewDTO::fromEntity).toList();

//             groupMap.put(c.getId(), TimelineGroupDTO.builder()
//                 .dateLabel("🧠 Semantic Group – " + c.getCreatedAt().toLocalDate())
//                 .chunks(previews)
//                 .build());
//         }

//         return new ArrayList<>(groupMap.values());
//     }
//     public List<TimelineGroupDTO> getSemanticTimeline(UUID userId, List<Float> embedding) {
//         List<Chunk> chunks = chunkRepo.findByUserId(userId);
//         Map<UUID, List<Chunk>> semanticGroups = new HashMap<>();

//         for (Chunk c : chunks) {
//             // Query similar chunks (exclude current chunk from result)
//             Map<String, String> filter = Map.of("user_id", userId.toString());
//             List<ScoredVectorWithUnsignedIndices> matches = pineconeService.queryVectors(
//                 embedding, 10, filter
//             );

//             List<Chunk> related = matches.stream()
//                 .filter(match -> !match.getId().equals(c.getId().toString())) // exclude self
//                 .map(match -> chunkRepo.findById(UUID.fromString(match.getId())).orElse(null))
//                 .filter(Objects::nonNull)
//                 .toList();

//             semanticGroups.put(c.getId(), related);
//         }

//         // Convert to TimelineGroupDTOs
//         return semanticGroups.entrySet().stream()
//             .map(e -> {
//                 Chunk leader = chunkRepo.findById(e.getKey()).orElseThrow();
//                 List<ChunkPreviewDTO> previews = Stream.concat(
//                         Stream.of(leader),
//                         e.getValue().stream()
//                 ).map(ChunkPreviewDTO::fromEntity).toList();

//                 return TimelineGroupDTO.builder()
//                         .dateLabel("🧠 Group for " + leader.getCreatedAt().toLocalDate())
//                         .chunks(previews)
//                         .build();
//             }).toList();
//     }
//     public List<TimelineGroupDTO> getTimeline(UUID userId, String groupBy) {
//         List<Chunk> chunks = chunkRepo.findByUserId(userId);

//         Map<String, List<Chunk>> grouped = switch (groupBy) {
//             case "week" -> groupByWeek(chunks);
//             case "month" -> groupByMonth(chunks);
//             default -> groupByDay(chunks);
//         };

//         return grouped.entrySet().stream()
//                 .sorted(Map.Entry.<String, List<Chunk>>comparingByKey(Comparator.reverseOrder()))
//                 .map((Map.Entry<String, List<Chunk>> e) -> {
//                     List<ChunkPreviewDTO> previews = e.getValue().stream()
//                             .map(ChunkPreviewDTO::fromEntity)
//                             .toList();

//                     return TimelineGroupDTO.builder()
//                             .dateLabel(e.getKey())
//                             .chunks(previews)
//                             .build();
//                 })
//                 .toList();
//     }

//     private Map<String, List<Chunk>> groupByDay(List<Chunk> chunks) {
//         return chunks.stream().collect(Collectors.groupingBy(
//                 c -> c.getCreatedAt().toLocalDate().toString()
//         ));
//     }

//     private Map<String, List<Chunk>> groupByWeek(List<Chunk> chunks) {
//         DateTimeFormatter fmt = DateTimeFormatter.ofPattern("YYYY-'W'ww");
//         return chunks.stream().collect(Collectors.groupingBy(
//                 c -> fmt.format(c.getCreatedAt())
//         ));
//     }

//     private Map<String, List<Chunk>> groupByMonth(List<Chunk> chunks) {
//         DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM yyyy");
//         return chunks.stream().collect(Collectors.groupingBy(
//                 c -> fmt.format(c.getCreatedAt())
//         ));
//     }
// }