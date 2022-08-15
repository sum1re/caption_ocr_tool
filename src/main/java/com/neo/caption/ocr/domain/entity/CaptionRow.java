package com.neo.caption.ocr.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class CaptionRow {

    private int startIndex;
    private int endIndex;
    private String caption;
    private Mat mat;

    public CaptionRowDto toDto() {
        return new CaptionRowDto(this.startTime, this.endTime, this.caption);
    }
}
