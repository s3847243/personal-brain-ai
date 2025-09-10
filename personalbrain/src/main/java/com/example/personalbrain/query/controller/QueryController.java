package com.example.personalbrain.query.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.personalbrain.query.service.DateRangeExtractor;
import com.example.personalbrain.query.service.GptStreamService;
import com.example.personalbrain.query.service.SemanticSearchService;
import com.example.personalbrain.user.model.ChatMessage;
import com.example.personalbrain.user.model.UserPrincipal;
import com.example.personalbrain.user.repository.ChatMessageRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/query")
@RequiredArgsConstructor
public class QueryController {

    private final SemanticSearchService semantic;
    private final GptStreamService gpt;
    private final DateRangeExtractor dateRangeExtractor;
    private final ChatMessageRepository chatMessageRepo;
    private final ThreadPoolTaskExecutor securityExecutor;

    @GetMapping(value = "/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter ask(@RequestParam UUID sessionId,
                        @RequestParam String q,
                        @AuthenticationPrincipal UserPrincipal user) {
        UUID userId = UUID.fromString(user.getUser().getId().toString());

        SseEmitter emitter = new SseEmitter(0L); 
        emitter.onTimeout(emitter::complete);
        emitter.onError(ex -> emitter.complete());
        emitter.onCompletion(() -> {});

        try {
            emitter.send(SseEmitter.event().name("open").data("ok"));
        } catch (IOException ignored) {}

        Runnable task = () -> {
            try {
                var savedUser = chatMessageRepo.save(
                    ChatMessage.builder().sessionId(sessionId).role("user").content(q).build()
                );
                try {
                emitter.send(SseEmitter.event().name("user-saved").data(
                    java.util.Map.of(
                        "id", savedUser.getId().toString(),
                        "role", "user",
                        "content", savedUser.getContent(),
                        "timestamp", savedUser.getCreatedAt().toString()
                    )
                ));
                } catch (Exception ignored) {}

                emitter.send(SseEmitter.event().name("status").data("fetching-history"));
                var history = chatMessageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);

                emitter.send(SseEmitter.event().name("status").data("extracting-date-range"));
                var range = dateRangeExtractor.extractDateRange(q);

                emitter.send(SseEmitter.event().name("status").data("retrieving-chunks"));
                var chunks = semantic.findRelevantChunks(q, userId, 6, range);
                emitter.send(SseEmitter.event().name("status").data("chunks:" + chunks.size()));

                emitter.send(SseEmitter.event().name("status").data("model-start"));
                gpt.streamWithMemory(history, chunks, q, emitter, sessionId);

            } catch (Exception e) {
                try { emitter.send(SseEmitter.event().name("error").data(e.getMessage())); } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
            };

            CompletableFuture.runAsync(task, securityExecutor);
            return emitter;
    }

}