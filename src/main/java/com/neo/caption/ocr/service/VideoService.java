package com.neo.caption.ocr.service;

import com.neo.caption.ocr.domain.entity.CaptionRow;
import com.neo.caption.ocr.domain.entity.TaskAttribute;
import org.opencv.videoio.VideoCapture;

import java.util.List;

public interface VideoService {

    Integer getFrameCount(String taskId);

    void removeFrameCount(String taskId);

    VideoCapture openVideoFile(String hash);

    List<CaptionRow> processVideo(TaskAttribute taskAttribute);

}
