package com.neo.caption.ocr.service;

import com.neo.caption.ocr.domain.dto.FileChecksumDto;
import com.neo.caption.ocr.domain.dto.FileChunkDto;

import java.io.File;
import java.nio.file.Path;

public interface FileService {

    <T> void saveJsonToFile(Path path, T object);

    <T> T readJsonFromFile(Path path, Class<T> tClass);

    String getFileHeader(File file);

    void createTempDirectory(String hash);

    void uploadFileChunk(FileChunkDto fileChunkDto);

    void combineFileChunk(FileChecksumDto fileChecksumDto);

    Path getHashFile(String hash);

    void removeHashCache(String hash);
}
