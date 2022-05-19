package com.neo.caption.ocr.domain.entity;

import com.neo.caption.ocr.domain.dto.CaptionRowDto;
import lombok.Data;
import org.opencv.core.Mat;

@Data
public class CaptionRow {

    private int id;
    private double startTime;
    private double endTime;
    private String caption;
    private Mat mat;

    public CaptionRowDto toDto() {
        return new CaptionRowDto(this.startTime, this.endTime, this.caption);
    }

}
