package com.example.personalbrain.ingestion.service;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.personalbrain.ingestion.model.ObjectStorage;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;


@Slf4j
@Service
@RequiredArgsConstructor
public class S3Storage implements ObjectStorage {
    @Value("${aws.s3.bucket}")          private String bucket;
    @Value("${aws.s3.signed-url-ttl}")  private int signedUrlTtl;
    // add s3 aws creds from jobpilot
    private final S3Client s3 = S3Client.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())    // env vars / ~/.aws/credentials
            .region(Region.of(System.getenv().getOrDefault("AWS_REGION", "ap-southeast-2")))
            .build();

    @PostConstruct
    void ensureBucket() {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("Created S3 bucket {}", bucket);
        }
    }
    
    @Override
    public String put(InputStream in, long size,String contentType, String filename) {
        String key = UUID.randomUUID() + "-" + filename;
                s3.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .contentType(contentType)
                                .build(),
                        RequestBody.fromInputStream(in, size)
                );
        return key;    
    }

  
    @Override
    public InputStream get(String key) {
        return s3.getObject(builder -> builder.bucket(bucket).key(key));
    }
    @Override
    public void delete(String key) {
        try {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            log.info("✅ Deleted S3 object with key: {}", key);
        } catch (Exception e) {
            log.error("❌ Failed to delete S3 object with key: {}", key, e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

}
