package com.neo.caption.ocr.service.impl;

import com.neo.caption.ocr.aspect.AopException;
import com.neo.caption.ocr.exception.ModuleException;
import com.neo.caption.ocr.pojo.AppHolder;
import com.neo.caption.ocr.pojo.VideoHolder;
import com.neo.caption.ocr.service.OpenCVService;
import com.neo.caption.ocr.service.VideoService;
import com.neo.caption.ocr.util.DecimalUtil;
import com.neo.caption.ocr.util.FxUtil;
import com.neo.caption.ocr.util.PrefUtil;
import com.neo.caption.ocr.view.MatNode;
import javafx.scene.control.ProgressBar;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.neo.caption.ocr.util.BaseUtil.convertTime;
import static org.opencv.videoio.Videoio.*;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {

    private final OpenCVService openCVService;
    private final VideoHolder videoHolder;
    private final PrefUtil prefUtil;
    private final FxUtil fxUtil;
    private final AppHolder appHolder;

    private VideoCapture vc;
    private int count;
    private boolean finish;
    private Mat mat;
    private Mat sampleMat;
    private List<ArchiveMatNode> archiveMatNodeList;

    public VideoServiceImpl(OpenCVService openCVService, VideoHolder videoHolder, PrefUtil prefUtil,
                            FxUtil fxUtil, AppHolder appHolder) {
        this.openCVService = openCVService;
        this.videoHolder = videoHolder;
        this.prefUtil = prefUtil;
        this.fxUtil = fxUtil;
        this.appHolder = appHolder;
    }

    @PostConstruct
    public void init() {
        this.vc = null;
        this.mat = new Mat();
        this.sampleMat = new Mat();
        this.archiveMatNodeList = new ArrayList<>(512);
    }

    @Override
    public Integer loadVideo(File videoFile) {
        this.vc = new VideoCapture(videoFile.getAbsolutePath());
        videoHolder.setWidth((int) vc.get(CAP_PROP_FRAME_WIDTH))
                .setHeight((int) vc.get(CAP_PROP_FRAME_HEIGHT))
                .setFps(vc.get(CAP_PROP_FPS))
                .setTotalFrame((int) vc.get(CAP_PROP_FRAME_COUNT) - 1)
                .setRatio(DecimalUtil.divide(videoHolder.getHeight(), videoHolder.getWidth()).doubleValue());
        this.count = 0;
        this.finish = false;
        archiveMatNodeList.clear();
        appHolder.getMatNodeList().clear();
        return 1;
    }

    @Override
    public boolean readFrame(Mat mat, double count) {
        if (!vc.isOpened()) {
            return false;
        }
        return vc.set(CAP_PROP_POS_FRAMES, count) && vc.read(mat);
    }

    @Override
    @AopException
    public void videoToCOCR(ProgressBar progressBar) throws ModuleException {
        if (!vc.isOpened()) {
            return;
        }
        boolean isSSIM = prefUtil.getSimilarityType() == 0;
        double threshold = isSSIM
                ? prefUtil.getMinSSIMThreshold()
                : prefUtil.getMinPSNRThreshold();
        double similarity;
        Mat dst;
        int frameInterval = prefUtil.getFrameInterval();
        while (vc.grab() && !Thread.currentThread().isInterrupted()) {
            fxUtil.onFXThread(progressBar.progressProperty(), (double) count / videoHolder.getTotalFrame());
            if (frameInterval != 1) {
                if (count % frameInterval != 0) {
                    count++;
                    continue;
                }
            }
            vc.retrieve(mat);
            dst = openCVService.filter(mat);
            double time = vc.get(CAP_PROP_POS_MSEC);
            int blackPixel = openCVService.countBlackPixel(dst);
            if (blackPixel > prefUtil.getMinPixelCount()) {
                if (sampleMat.empty()) {
                    addToMergeGroup(time, dst, blackPixel);
                } else {
                    similarity = isSSIM
                            ? openCVService.meanSSIM(sampleMat, dst).val[0]
                            : openCVService.psnr(sampleMat, dst);
                    if (similarity > threshold) {
                        archiveMatNodeList.add(new ArchiveMatNode(count, time, dst, blackPixel));
                    } else {
                        mergeArchiveMatNode();
                        addToMergeGroup(time, dst, blackPixel);
                    }
                }
            }
            mat.release();
            count++;
        }
        mergeArchiveMatNode();
        if (!vc.grab()) {
            finish = true;
        }
        appHolder.setMatNodeList(appHolder.getMatNodeList()
                .stream()
                .sorted(Comparator.comparingInt(MatNode::getNid))
                .collect(Collectors.toList()));
    }

    @Override
    public boolean isVideoLoaded() {
        return vc != null && vc.isOpened();
    }

    @Override
    public boolean isVideoFinish() {
        return finish;
    }

    @Override
    public void closeVideo() {
        if (vc != null && vc.isOpened()) {
            vc.release();
            vc = null;
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
            switch (prefUtil.getStoragePolicy()) {
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
                    switch (prefUtil.getStoragePolicy()) {
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
