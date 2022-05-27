package com.neo.caption.ocr.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.neo.caption.ocr.constant.CacheKeyPrefix;
import com.neo.caption.ocr.constant.ErrorCode;
import com.neo.caption.ocr.domain.dto.FileChecksumDto;
import com.neo.caption.ocr.domain.dto.FileChunkDto;
import com.neo.caption.ocr.exception.BusinessException;
import com.neo.caption.ocr.service.CacheService;
import com.neo.caption.ocr.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;

import static com.neo.caption.ocr.util.BaseUtil.v2s;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Qualifier("dotJoiner")
    private final Joiner dotJoiner;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;

    private StringBuilder stringBuilder;

    @PostConstruct
    public void init() {
        this.stringBuilder = new StringBuilder(8);
    }

    @Override
    public <T> void saveJsonToFile(Path path, T object) {
        try (var writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
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
        var queryCachePath = getHashDirectory(hash);
        if (queryCachePath != null) {
            try (var stream = Files.walk(queryCachePath)) {
                stream.sorted(Comparator.reverseOrder())
                        .forEach(this::deleteFile);
            } catch (NoSuchFileException ignored) {
                // do nothing
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.FAILED_PRECONDITION, e);
            }
            removeHashCache(hash);
        }
        try {
            var hashDirectory = cacheService.putCache(
                    CacheKeyPrefix.HASH_DIRECTORY, hash, Files.createTempDirectory(""));
            hashDirectory.toFile().deleteOnExit();
            log.info("hash: {}, path: {}", hash, hashDirectory);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ABORTED, e);
        }
    }

    @Override
    public void uploadFileChunk(@NotNull FileChunkDto fileChunkDto) {
        if (fileChunkDto.index() == null || fileChunkDto.file() == null || fileChunkDto.file().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT);
        }
        var path = getHashDirectory(fileChunkDto.hash());
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
        var hashDirectory = getHashDirectory(fileChecksumDto.hash());
        if (hashDirectory == null) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT);
        }
        var combinePath = getHashFile(fileChecksumDto.hash());
        var combineStandardOpenOptions = new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND};
        try (var pathStream = Files.list(hashDirectory).filter(path -> path.toString().endsWith(".tmp"));
             var combineChannel = FileChannel.open(combinePath, combineStandardOpenOptions)) {
            var list = pathStream.sorted().toList();
            if (list.size() != fileChecksumDto.totalChunk()) {
                throw new BusinessException(ErrorCode.DATA_LOSS);
            }
            var start = 0;
            for (var path : list) {
                try (var fileChannel = FileChannel.open(path)) {
                    combineChannel.transferFrom(fileChannel, start, fileChannel.size());
                    start += fileChannel.size();
                    deleteFile(path);
                }
            }
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
    public Path getHashFile(String hash) {
        var hashDirectory = getHashDirectory(hash);
        if (hashDirectory == null) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT);
        }
        return hashDirectory.resolve(hash);
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

    /**
     * Get the path from cache by the given hash.
     *
     * @param hash file hash, its temporary-file directory should exist, <br/>
     *             otherwise it will throw INVALID_ARGUMENT.
     * @return the hash temporary-file directory
     */
    private Path getHashDirectory(String hash) {
        return cacheService.getCache(CacheKeyPrefix.HASH_DIRECTORY, hash);
    }

    @Override
    public void removeHashCache(String hash) {
        cacheService.removeCache(CacheKeyPrefix.HASH_DIRECTORY, hash);
    }

    private void deleteFile(Path target) {
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FAILED_PRECONDITION, e);
        }
    }

}
