package com.neo.caption.ocr.service;

import com.neo.caption.ocr.domain.dto.OCRDto;
import com.neo.caption.ocr.domain.dto.TaskDto;
import com.neo.caption.ocr.domain.entity.CaptionRow;
import org.opencv.videoio.VideoCapture;

import java.io.File;

public interface VideoService {

    Integer getFrameCount(String taskId);

    void removeFrameCount(String taskId);

    VideoCapture openVideoFile(String hash);

    boolean isVideoFinish();

    void closeVideo();

}
