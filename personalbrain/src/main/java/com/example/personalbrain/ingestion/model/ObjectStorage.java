package com.example.personalbrain.ingestion.model;

import java.io.InputStream;
import java.net.URL;

public interface ObjectStorage {
    String put(InputStream in, long size, String contentType, String filename);
    // URL presignedGet(String key);
    InputStream get(String key);
    void delete(String key);

}