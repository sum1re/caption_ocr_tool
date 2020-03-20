package com.neo.caption.ocr.service;

import com.neo.caption.ocr.exception.ModuleException;
import javafx.scene.control.ProgressBar;
import org.opencv.core.Mat;

import java.io.File;

public interface VideoService {

    boolean readFrame(Mat mat, double count);

    Integer loadVideo(File videoFile);

    void videoToCOCR(ProgressBar progressBar) throws ModuleException;

    boolean isVideoLoaded();

    boolean isVideoFinish();

    void closeVideo();

}
