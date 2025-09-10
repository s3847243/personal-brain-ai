package com.example.personalbrain.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.personalbrain.user.model.ChatSession;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<ChatSession> findByIdAndUserId(UUID id, UUID userId);
    // bulk delete derived query (returns # rows deleted) need to change ChatMessage and ChatSession Mapping
}
