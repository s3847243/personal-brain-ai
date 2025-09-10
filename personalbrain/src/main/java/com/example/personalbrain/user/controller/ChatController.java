package com.example.personalbrain.user.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.personalbrain.user.model.ChatMessage;
import com.example.personalbrain.user.model.ChatSession;
import com.example.personalbrain.user.model.UserPrincipal;
import com.example.personalbrain.user.repository.ChatMessageRepository;
import com.example.personalbrain.user.repository.ChatSessionRepository;
import com.example.personalbrain.user.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatSessionRepository chatSessionRepo;
    private final ChatMessageRepository chatMessageRepo;
    private final ChatService chatService;
    @PostMapping("/start")
    public ChatSession startSession(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserPrincipal user) {
        UUID userId = UUID.fromString(user.getUser().getId().toString());
        String title = body.getOrDefault("title", "New Chat");

        ChatSession session = ChatSession.builder()
            .userId(userId)
            .title(title)
            .build();
        
        return chatSessionRepo.save(session);
    }
    @GetMapping("/sessions")
    public List<ChatSession> listSessions(@AuthenticationPrincipal UserPrincipal user) {
        UUID userId = UUID.fromString(user.getUser().getId().toString());
        return chatSessionRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }
    @GetMapping("/sessions/{sessionId}")
    public ChatSession getChatSessionInformation(@AuthenticationPrincipal UserPrincipal user, @PathVariable UUID sessionId) {
        UUID userId = UUID.fromString(user.getUser().getId().toString());
        return chatSessionRepo.findByIdAndUserId(sessionId, userId )
            .orElseThrow(() -> new RuntimeException("Chat session not found or does not belong to user"));
    }
    @GetMapping("/{sessionId}/messages")
    public List<ChatMessage> getMessages(@PathVariable UUID sessionId) {
        return chatMessageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteChatSession(
        @PathVariable UUID sessionId,
        @AuthenticationPrincipal UserPrincipal principal) {

    UUID userId = principal.getUser().getId();
    chatService.deleteChatSession(userId, sessionId);
    return ResponseEntity.noContent().build();
    }

    
    
}
