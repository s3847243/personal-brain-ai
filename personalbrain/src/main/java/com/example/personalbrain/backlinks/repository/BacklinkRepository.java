package com.example.personalbrain.backlinks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.personalbrain.backlinks.model.Backlink;

import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface BacklinkRepository extends JpaRepository<Backlink, UUID> {
    List<Backlink> findByChunkId(UUID chunkId);
    List<Backlink> findByChunkIdIn(List<UUID> chunkIds);
    @Transactional
    @Modifying
    @Query("delete from Backlink b where b.chunkId in :ids or b.relatedChunkId in :ids")
    int deleteAllByAnyChunkIdIn(@Param("ids") Collection<UUID> ids);

}