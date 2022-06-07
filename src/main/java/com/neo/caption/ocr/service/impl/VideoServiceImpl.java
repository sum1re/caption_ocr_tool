package com.neo.caption.ocr.service.impl;

import com.neo.caption.ocr.constant.CacheKeyPrefix;
import com.neo.caption.ocr.constant.ErrorCode;
import com.neo.caption.ocr.domain.dto.OCRDto;
import com.neo.caption.ocr.domain.dto.TaskDto;
import com.neo.caption.ocr.domain.entity.CaptionRow;
import com.neo.caption.ocr.exception.BusinessException;
import com.neo.caption.ocr.property.OCRProperties;
import com.neo.caption.ocr.service.CacheService;
import com.neo.caption.ocr.service.FileService;
import com.neo.caption.ocr.service.VideoService;
import com.neo.caption.ocr.util.DecimalUtil;
import com.neo.caption.ocr.util.OpenCVUtil;
import com.neo.caption.ocr.util.opencv.Crop;
import com.neo.caption.ocr.util.opencv.Process;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.opencv.videoio.Videoio.CAP_PROP_POS_AVI_RATIO;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final FileService fileService;
    private final CacheService cacheService;
    private final OCRProperties ocrProperties;

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
    public void closeVideo() {
        if (videoCapture != null && videoCapture.isOpened()) {
            videoCapture.release();
            System.gc();
        }
    }

    private void addToMergeGroup(final double time, final Mat mat, final int blackPixel) {
        mat.copyTo(sampleMat);
        archiveMatNodeList.add(new ArchiveMatNode(count, time, mat, blackPixel));
    }

    private void mergeArchiveMatNode() {
        ArchiveMatNode archiveMatNode;
        int size = archiveMatNodeList.size();
        if (size == 0) {
            return;
        }
        if (size > 1) {
            switch (STORAGE_POLICY.intValue()) {
                //STORAGE_FIRST
                case 3:
                    archiveMatNode = archiveMatNodeList.get(0);
                    break;
                //STORAGE_LAST
                case 4:
                    archiveMatNode = archiveMatNodeList.get(size - 1);
                    break;
                //MIN/MAX/MED
                default:
                    List<ArchiveMatNode> tp = archiveMatNodeList
                            .stream()
                            .sorted(Comparator.comparingInt(ArchiveMatNode::getPixelCount))
                            .collect(Collectors.toList());
                    switch (STORAGE_POLICY.intValue()) {
                        //STORAGE_MIN
                        case 0:
                            archiveMatNode = tp.get(0);
                            break;
                        //STORAGE_MAX
                        case 1:
                            archiveMatNode = tp.get(size - 1);
                            break;
                        //STORAGE_MED
                        default:
                            archiveMatNode = tp.get(size % 2 == 0 ? size / 2 : size / 2 - 1);
                            break;
                    }
                    break;
            }
        } else {
            archiveMatNode = archiveMatNodeList.get(0);
        }
        MatNode matNode = archiveMatNode.getMatNode();
        double startTime = archiveMatNodeList.get(0).getMatNode().getStartTime();
        double endTime = archiveMatNodeList.get(size - 1).getMatNode().getStartTime();
        matNode.setStartTime(startTime);
        matNode.setEndTime(endTime);
        log.debug("System merge. Start at {} ({}), end at {} ({}), last {} FrameInterval.",
                archiveMatNodeList.get(0).getMatNode().getNid(),
                convertTime(startTime),
                archiveMatNodeList.get(archiveMatNodeList.size() - 1).getMatNode().getNid(),
                convertTime(endTime),
                archiveMatNodeList.size());
        appHolder.getMatNodeList().add(matNode);
        archiveMatNodeList.clear();
    }

    @Getter
    @Setter
    public static class ArchiveMatNode {

        private final MatNode matNode;
        private final int pixelCount;

        ArchiveMatNode(int count, double time, Mat mat, int pixelCount) {
            this(new MatNode(count, time, mat), pixelCount);
        }

        ArchiveMatNode(MatNode matNode, int pixelCount) {
            this.matNode = matNode;
            this.pixelCount = pixelCount;
        }

    }

}
