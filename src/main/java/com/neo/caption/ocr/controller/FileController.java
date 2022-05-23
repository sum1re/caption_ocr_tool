package com.neo.caption.ocr.controller;

import com.neo.caption.ocr.domain.dto.FileChecksumDto;
import com.neo.caption.ocr.domain.dto.FileChunkDto;
import com.neo.caption.ocr.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@Slf4j
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/{hash}")
    public ResponseEntity<Object> initialize(@PathVariable String hash) {
        fileService.createTempDirectory(hash);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{hash}/{index}")
    public ResponseEntity<Object> uploadChunk(
            @PathVariable String hash, @PathVariable Integer index, @RequestPart(name = "file") MultipartFile file) {
        var fileChunk = new FileChunkDto(hash, index, file);
        fileService.uploadFileChunk(fileChunk);
        return ResponseEntity.ok().build();
    }

    @PatchMapping
    public ResponseEntity<Object> combine(@RequestBody FileChecksumDto fileChecksumDto) {
        fileService.combineFileChunk(fileChecksumDto);
        return ResponseEntity.ok().build();
    }

}
