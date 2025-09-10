package com.example.personalbrain.ingestion.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.personalbrain.ingestion.service.FileIngestionService;
import com.example.personalbrain.user.model.UserPrincipal;

import lombok.RequiredArgsConstructor;

import com.example.personalbrain.ingestion.model.Document;
@RestController
@RequestMapping("/api/ingest")
@RequiredArgsConstructor
public class IngestionController {
    private final FileIngestionService fileService;

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Document uploadFile(@RequestPart("file") MultipartFile file,
                               @AuthenticationPrincipal UserPrincipal user) {
        return fileService.handleUpload(file, user);
    }

}
