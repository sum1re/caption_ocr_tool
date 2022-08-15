package com.neo.caption.ocr.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.neo.caption.ocr.databind.TimelineSerialize;

import java.math.BigDecimal;

public record CaptionRowDto(
        @JsonSerialize(using = TimelineSerialize.class) BigDecimal startTime,
        @JsonSerialize(using = TimelineSerialize.class) BigDecimal endTime,
        String caption) {
}
