package com.example.personalbrain.user.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.personalbrain.user.model.ChatSession;
import com.example.personalbrain.user.repository.ChatMessageRepository;
import com.example.personalbrain.user.repository.ChatSessionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {
  private final ChatSessionRepository chatSessionRepo;
  private final ChatMessageRepository chatMessageRepo;
  @Transactional
  public void deleteChatSession(UUID userId, UUID sessionId) {
    ChatSession session = chatSessionRepo.findByIdAndUserId(sessionId, userId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Chat session not found or not owned"));

    chatMessageRepo.deleteBySessionId(session.getId()); // transactional now
    chatSessionRepo.delete(session);
  }
}