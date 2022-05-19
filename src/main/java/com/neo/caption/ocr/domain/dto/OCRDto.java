package com.neo.caption.ocr.domain.dto;

public record OCRDto(int upperLeftX, int upperLeftY, int lowerRightX, int lowerRightY, String language) {
}
