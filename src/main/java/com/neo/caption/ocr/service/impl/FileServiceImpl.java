package com.neo.caption.ocr.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.neo.caption.ocr.constant.ErrorCode;
import com.neo.caption.ocr.domain.dto.FileChecksumDto;
import com.neo.caption.ocr.domain.dto.FileChunkDto;
import com.neo.caption.ocr.exception.BusinessException;
import com.neo.caption.ocr.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.neo.caption.ocr.util.BaseUtil.v2s;

@Service
@Slf4j
@RequiredArgsConstructor
@CacheConfig(cacheNames = "file-path")
public class FileServiceImpl implements FileService {

    @Qualifier("dotJoiner")
    private final Joiner dotJoiner;
    private final ObjectMapper objectMapper;

    private StringBuilder stringBuilder;
    private Map<String, Path> hashPathMap;

    @PostConstruct
    public void init() {
        this.hashPathMap = new HashMap<>(4);
        this.stringBuilder = new StringBuilder(8);
    }

    @Override
    public <T> void saveJsonToFile(Path path, T object) {
        try (var writer = Files.newBufferedWriter(path)) {
            objectMapper.writeValue(writer, object);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, e);
        }
    }

    @Override
    public <T> T readJsonFromFile(Path path, Class<T> tClass) {
        try (var reader = Files.newBufferedReader(path)) {
            return objectMapper.readValue(reader, tClass);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, e);
        }
    }

    @Override
    public String getFileHeader(File file) {
        try (var fileInputStream = new FileInputStream(file)) {
            var bytes = new byte[4];
            //noinspection ResultOfMethodCallIgnored
            fileInputStream.read(bytes, 0, bytes.length);
            return bytesToHex(bytes);
        } catch (Exception ignored) {
            return "";
        }
    }

    /**
     * Creates a new temporary-file directory, if the directory exists, it will be deleted and recreated.
     * <br/>
     * It will be deleted when the virtual machine terminates.
     *
     * @param hash file hash<br/>
     *             If the hash is empty or null, it will throw INVALID_ARGUMENT.<br/>
     *             If an I/O error occurs or the temporary-file directory doesn't exist, it will throw ABORTED
     */
    @Override
    public void createTempDirectory(String hash) {
        if (Strings.isNullOrEmpty(hash)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT);
        }
        if (hashPathMap.containsKey(hash)) {
            var tempPath = hashPathMap.getOrDefault(hash, null);
            if (tempPath != null && Files.exists(tempPath)) {
                deleteDirectory(tempPath);
            }
            hashPathMap.remove(hash);
        }
        try {
            var hashPath = Files.createTempDirectory("");
            hashPath.toFile().deleteOnExit();
            hashPathMap.put(hash, hashPath);
            log.info("{}: {}", hash, hashPath);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ABORTED, e);
        }
    }

    @Override
    public void uploadFileChunk(@NotNull FileChunkDto fileChunkDto) {
        if (fileChunkDto.index() == null || fileChunkDto.file() == null || fileChunkDto.file().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT);
        }
        var path = getPathByHash(fileChunkDto.hash());
        var fileIndex = Strings.padStart(v2s(fileChunkDto.index()), 5, '0');
        var savePath = path.resolve(dotJoiner.join(fileIndex, "tmp"));
        try {
            fileChunkDto.file().transferTo(savePath);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ABORTED, e);
        }
    }

    @Override
    public void combineFileChunk(@NotNull FileChecksumDto fileChecksumDto) {
        var hashTempPath = getPathByHash(fileChecksumDto.hash());
        var combinePath = getFileByHash(fileChecksumDto.hash());
        var combineStandardOpenOptions = new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND};
        try (var pathStream = Files.list(hashTempPath).filter(path -> path.toString().endsWith(".tmp"));
             var combineChannel = FileChannel.open(combinePath, combineStandardOpenOptions)) {
            var list = pathStream.sorted().toList();
            if (list.size() != fileChecksumDto.totalChunk()) {
                throw new BusinessException(ErrorCode.DATA_LOSS);
            }
            combinePathList(combineChannel, list);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ABORTED, e);
        }
    }

    /**
     * Get the file path by the given hash.<br/><br/>
     * If the given hash is 'aaa' and its path is 'aaa-000',
     * the result is 'aaa-000/aaa'.
     *
     * @param hash file hash, its temporary-file directory should exist, <br/>
     *             otherwise it will throw INVALID_ARGUMENT.
     * @return the file path
     */
    @Override
    @Cacheable(key = "#hash")
    public Path getFileByHash(String hash) {
        var hashTempPath = getPathByHash(hash);
        return hashTempPath.resolve(hash);
    }

    private @NotNull String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        stringBuilder.setLength(0);
        var hex = "";
        for (byte b : bytes) {
            hex = Integer.toHexString(b & 0xFF).toUpperCase();
            if (hex.length() < 2)
                stringBuilder.append(0);
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }

    private void combinePathList(FileChannel outChannel, @NotNull List<Path> chunkList) throws IOException {
        var start = 0;
        for (var path : chunkList) {
            try (var fileChannel = FileChannel.open(path)) {
                outChannel.transferFrom(fileChannel, start, fileChannel.size());
                start += fileChannel.size();
                Files.delete(path);
            }
        }
    }

    /**
     * Get the path from 'hashPathMap' by the given hash.
     *
     * @param hash file hash, its temporary-file directory should exist, <br/>
     *             otherwise it will throw INVALID_ARGUMENT.
     * @return the hash temporary-file directory
     */
    private @NotNull Path getPathByHash(String hash) {
        if (Strings.isNullOrEmpty(hash)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT);
        }
        var path = hashPathMap.getOrDefault(hash, null);
        if (path == null || !Files.exists(path)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT);
        }
        return path;
    }

    private void deleteDirectory(@NotNull Path target) {
        if (!Files.exists(target)) {
            return;
        }
        try (var stream = Files.walk(target)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(this::deleteFile);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FAILED_PRECONDITION, e);
        }
    }

    private void deleteFile(Path target) {
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FAILED_PRECONDITION, e);
        }
    }

}
