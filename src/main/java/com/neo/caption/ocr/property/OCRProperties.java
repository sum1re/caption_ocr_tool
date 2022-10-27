package com.neo.caption.ocr.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@ConfigurationProperties(prefix = "ocr.config")
public class OCRProperties {

    @Min(0)
    @Max(1)
    private double minBlackPixelThreshold;

    @Min(0)
    @Max(1)
    private double maxWhitePixelThreshold;

    @Min(0)
    @Max(1)
    private double ssimThreshold;

    @Min(0)
    @Max(1)
    private double invertThreshold;

}
