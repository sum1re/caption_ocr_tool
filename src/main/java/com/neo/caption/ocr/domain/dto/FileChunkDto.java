package com.neo.caption.ocr.domain.dto;

import org.springframework.web.multipart.MultipartFile;

public record FileChunkDto(String hash, Integer index, MultipartFile file) {
}
