package com.example.personalbrain.timeline.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.personalbrain.timeline.dto.TimelineGroupDTO;
import com.example.personalbrain.user.model.UserPrincipal;

import lombok.RequiredArgsConstructor;

// @RestController
// @RequestMapping("/api/timeline")
// @RequiredArgsConstructor
// public class TimelineController {

//     private final TimelineService service;

//     @GetMapping
//     public List<TimelineGroupDTO> getTimeline(@RequestParam(defaultValue = "day") String group,
//                                               @AuthenticationPrincipal UserPrincipal user) {
//         UUID userId = UUID.fromString(user.getName()); // adjust based on auth
//         return service.getTimeline(userId, group);
//     }
//     @GetMapping("/semantic")
//     public ResponseEntity<List<TimelineGroupDTO>> semanticTimeline(
//             @AuthenticationPrincipal UserPrincipal user) {
//         List<TimelineGroupDTO> groups = service.getSemanticTimeline(user.getUser().getId());
//         return ResponseEntity.ok(groups);
//     }
// }