package com.example.personalbrain.ingestion.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TextExtractor {
    private final AutoDetectParser parser = new AutoDetectParser();

    
     public String extract(InputStream inputStream) {
        try {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1); // unlimited
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();
        
        System.out.println("Stream type: " + inputStream.getClass().getSimpleName());
        System.out.println("Mark supported: " + inputStream.markSupported());
        
        // Parse the document
        parser.parse(inputStream, handler, metadata, context);
        
        String extractedText = handler.toString();
        
        // Print metadata information
        System.out.println("Content-Type: " + metadata.get("Content-Type"));
        System.out.println("Title: " + metadata.get("title"));
        System.out.println("Creator: " + metadata.get("creator"));
        System.out.println("Pages: " + metadata.get("xmpTPg:NPages"));
        
        // Print all metadata for debugging
        System.out.println("\nAll Metadata:");
        for (String name : metadata.names()) {
            System.out.println("  " + name + ": " + metadata.get(name));
        }
        
        return extractedText;
        } catch (Exception e) {
            log.error("Text extraction failed", e);
            throw new RuntimeException("Failed to extract text from file", e);
        }
    }
    
    

}
