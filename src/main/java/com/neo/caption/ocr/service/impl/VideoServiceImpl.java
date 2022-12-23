package com.neo.caption.ocr.service.impl;

import com.neo.caption.ocr.constant.CacheKeyPrefix;
import com.neo.caption.ocr.constant.ErrorCode;
import com.neo.caption.ocr.domain.entity.CRect;
import com.neo.caption.ocr.domain.entity.CaptionRow;
import com.neo.caption.ocr.domain.entity.TaskAttribute;
import com.neo.caption.ocr.exception.BusinessException;
import com.neo.caption.ocr.property.OCRProperties;
import com.neo.caption.ocr.service.*;
import com.neo.caption.ocr.util.DecimalUtil;
import com.neo.caption.ocr.util.OpenCVUtil;
import com.neo.caption.ocr.util.opencv.Crop;
import com.neo.caption.ocr.util.opencv.Process;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.features2d.MSER;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Range.atLeast;
import static com.google.common.collect.Range.closed;
import static org.opencv.videoio.Videoio.CAP_PROP_POS_AVI_RATIO;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final FileService fileService;
    private final CacheService cacheService;
    private final OCRProperties ocrProperties;
    private final TesseractService tesseractService;

    private void putFrameCount(String taskId, int count) {
        cacheService.putCache(CacheKeyPrefix.FRAME_COUNT, taskId, count);
    }

    @Override
    public Integer getFrameCount(String taskId) {
        return cacheService.getCache(CacheKeyPrefix.FRAME_COUNT, taskId);
    }

    @Override
    public void removeFrameCount(String taskId) {
        cacheService.removeCache(CacheKeyPrefix.FRAME_COUNT, taskId);
    }

    @Override
    public @NotNull VideoCapture openVideoFile(String hash) {
        var videoPath = fileService.getHashFile(hash);
        var videoCapture = new VideoCapture(videoPath.toString());
        if (!videoCapture.isOpened()) {
            throw new BusinessException(ErrorCode.FAILED_PRECONDITION);
        }
        return videoCapture;
    }

    @Override
    public List<CaptionRow> processVideo(@NotNull TaskAttribute taskAttribute) {
        var videoCapture = openVideoFile(taskAttribute.getHash());
        var frameMat = new Mat();
        // create the crop object
        var crop = new Crop(
                taskAttribute.getUpperLeftX(), taskAttribute.getUpperLeftY(),
                taskAttribute.getLowerRightX(), taskAttribute.getLowerRightY());
        try {
            videoCapture.read(frameMat);
            crop.validateParam(frameMat);
        } catch (CvException e) {
            videoCapture.release();
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT, e);
        }
        var api = tesseractService.initTessBaseApi(taskAttribute.getLanguage());
        var ocrService = new OCRService(api);
        videoCapture.set(CAP_PROP_POS_AVI_RATIO, 0);
        // create captionRow
        var captionRow = new CaptionRow(0, 0, "");
        // create captionRow list
        var captionRowList = new ArrayList<CaptionRow>(1 << 10);
        var sampleMat = new Mat();
        var ocrMat = new Mat();
        var processUtil = new Object() {

            boolean initial = false;
            long totalFrame;
            int width;
            int height;
            int whiteThreshold;
            int blackThreshold;

            void init(@NotNull Mat mat) {
                this.totalFrame = mat.total();
                this.whiteThreshold = DecimalUtil.multiply(
                        totalFrame, ocrProperties.getMaxWhitePixelThreshold()).intValue();
                this.blackThreshold = DecimalUtil.multiply(
                        totalFrame, ocrProperties.getMinBlackPixelThreshold()).intValue();
                this.width = mat.cols();
                this.height = mat.rows();
                this.initial = true;
            }

            void merge(int index) {
                var text = ocrService.doOCR(sampleMat);
                captionRow.setEndIndex(index)
                        .setCaption(text);
                captionRowList.add(captionRow.clone());
            }

            void sample(int index) {
                ocrMat.copyTo(sampleMat);
                captionRow.setStartIndex(index);
            }

        };
        var processFrame = new Process() {
            // TODO: write the process
            @Override
            public void process(Mat mat) {
                var regions = new ArrayList<MatOfPoint>();
                var boxes = new MatOfRect();
                var mser = MSER.create(8, 40, 1600);
                mser.detectRegions(mat, regions, boxes);
                var set = regions.stream()
                        .map(Imgproc::boundingRect)
                        .map(CRect::bound)
                        .filter(rect -> closed(5, 40).contains(rect.height))
                        .filter(rect -> atLeast(5).contains(rect.width))
                        .sorted(Comparator.comparingInt(rect -> rect.x))
                        .distinct()
                        .toList();
            }

        };

        for (var index = 0; ; index++) {
            if (!videoCapture.read(frameMat)) {
                processUtil.merge(index);
                break;
            }
            log.info("get {} frame", index);
            putFrameCount(taskAttribute.getId(), index);
            // clone cropped mat to ocrMat
            crop.cloneProcess(frameMat).copyTo(ocrMat);
            if (!processUtil.initial) {
                processUtil.init(ocrMat);
            }
            // process ocrMat
            processFrame.process(ocrMat);
            var whitePixel = OpenCVUtil.countWhitePixel(ocrMat);
            var blackPixel = processUtil.totalFrame - whitePixel;
            // too many white pixels or too few black pixels
            if (whitePixel > processUtil.whiteThreshold || blackPixel < processUtil.blackThreshold) {
                // if sample is not empty, merge the previous frame, and skip the current frame
                if (!sampleMat.empty()) {
                    processUtil.merge(index - 1);
                    sampleMat.release();
                }
                continue;
            }
            // set sampleMat
            if (sampleMat.empty()) {
                processUtil.sample(index);
                continue;
            }
            // calculate the ssim between sampleMat and ocrMat
            var similarity = OpenCVUtil.ssim(sampleMat, ocrMat);
            // larger than ssimThreshold, add to temp list
            if (similarity > ocrProperties.getSsimThreshold()) {
                log.info("This frame is similar to the sample");
                continue;
            }
            // lower than ssimThreshold, merge temp list and add to ocr list
            processUtil.merge(index - 1);
            // reset sampleMat
            processUtil.sample(index);
        }
        videoCapture.release();
        OpenCVUtil.release(frameMat, sampleMat, ocrMat);
        putFrameCount(taskAttribute.getId(), -1);
        return captionRowList;
    }

}
