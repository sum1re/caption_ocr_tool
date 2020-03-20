package com.neo.caption.ocr.pojo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

/**
 * to hold some information of the video file
 */
@Accessors(chain = true)
@Component
@Data
public class VideoHolder {

    private Integer width;
    private Integer height;
    private Double ratio;
    private double fps;
    private int totalFrame;

}
