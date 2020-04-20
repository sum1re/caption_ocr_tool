package com.neo.caption.ocr.service.impl;

import com.google.common.base.Joiner;
import com.neo.caption.ocr.aspect.AopException;
import com.neo.caption.ocr.constant.ModuleType;
import com.neo.caption.ocr.exception.ModuleException;
import com.neo.caption.ocr.pojo.AppHolder;
import com.neo.caption.ocr.pojo.ModuleStatus;
import com.neo.caption.ocr.service.ModuleService;
import com.neo.caption.ocr.service.OpenCVService;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.neo.caption.ocr.constant.PrefKey.MODULE_PROFILE_STATUS_LIST;
import static java.lang.StrictMath.log10;
import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.imgproc.Imgproc.*;

@Service
@Slf4j
public class OpenCVServiceImpl implements OpenCVService {

    private final ModuleService moduleService;
    private final AppHolder appHolder;
    private final ResourceBundle resourceBundle;
    private final Joiner joiner;

    private String moduleMsg;
    private Mat videoOriMat;
    private MinAreaUtil minAreaUtil;
    private Map<String, Integer> pixelColorMap;
    private Map<Integer, Mat> cacheMap;
    private boolean regionBoard;
    private int cropWidth;
    private int cropHeight;
    private int upperLeftX;
    private int upperLeftY;
    private int lowerRightX;
    private int lowerRightY;

    public OpenCVServiceImpl(ModuleService moduleService, AppHolder appHolder,
                             ResourceBundle resourceBundle, @Qualifier("dot") Joiner joiner) {
        this.moduleService = moduleService;
        this.appHolder = appHolder;
        this.resourceBundle = resourceBundle;
        this.joiner = joiner;
    }

    @PostConstruct
    public void init() {
        this.pixelColorMap = new HashMap<>();
        this.cacheMap = new HashMap<>();
        this.minAreaUtil = new MinAreaUtil();
        this.regionBoard = false;
        this.moduleMsg = resourceBundle.getString("exception.msg.module");
    }

    @Override
    public Mat spliceMatList() {
        Mat mat = appHolder.getMatNodeList().get(0).getMat();
        int width = mat.cols();
        int height = mat.rows();
        int len = appHolder.getMatNodeList().size();
        Mat result = new Mat(height * len, width, mat.depth());
        for (int i = 0; i < len; i++) {
            mat = appHolder.getMatNodeList().get(i).getMat();
            byte[] bytes = mat2ByteArrayByGet(mat);
            //byte[] bytes = mat2ByteArray(mat);
            result.put(i * height, 0, bytes);
        }
        return result;
    }

    @Override
    public byte[] mat2ByteArrayByGet(Mat mat) {
        byte[] bytes = new byte[(int) (mat.total() * mat.channels())];
        mat.get(0, 0, bytes);
        return bytes;
    }

    @Override
    public Image mat2Image(Mat mat, boolean isCompressImage) {
        InputStream is = new ByteArrayInputStream(mat2ByteArrayByMatOfByte(mat));
        return isCompressImage
                ? new Image(is, mat.width() / 2.0, mat.height() / 2.0, true, false)
                : new Image(is);
    }

    @Override
    public byte[] mat2ByteArrayByMatOfByte(Mat mat) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, matOfByte);
        return matOfByte.toArray();
    }

    @Override
    public Map<String, Integer> getPixelColor(int x, int y) {
        pixelColorMap.put("#x", x);
        pixelColorMap.put("#y", y);
        double[] bgr = videoOriMat.get(y, x);
        pixelColorMap.put("#rgB", (int) bgr[0]);
        pixelColorMap.put("#rGb", (int) bgr[1]);
        pixelColorMap.put("#Rgb", (int) bgr[2]);
        Mat pixel = new Mat(1, 1, CvType.CV_8UC3, new Scalar(bgr));
        Mat hsvPixel = new Mat(1, 1, CvType.CV_8UC3);
        Mat hlsPixel = new Mat(1, 1, CvType.CV_8UC3);
        Imgproc.cvtColor(pixel, hsvPixel, COLOR_BGR2HSV);
        Imgproc.cvtColor(pixel, hlsPixel, COLOR_BGR2HLS);
        double[] hsv = hsvPixel.get(0, 0);
        double[] hls = hlsPixel.get(0, 0);
        pixelColorMap.put("#Hsv", (int) hsv[0]);
        pixelColorMap.put("#hSv", (int) hsv[1]);
        pixelColorMap.put("#hsV", (int) hsv[2]);
        pixelColorMap.put("#Hls", (int) hls[0]);
        pixelColorMap.put("#hLs", (int) hls[1]);
        pixelColorMap.put("#hlS", (int) hls[2]);
        release(pixel, hsvPixel, hlsPixel);
        return pixelColorMap;
    }

    @Override
    public void setVideoOriMat(Mat mat) {
        this.videoOriMat = mat.clone();
    }

    @Override
    @AopException
    public Mat replaceRoiImage(Mat mat) throws ModuleException {
        Mat fin = filter(mat);
        Mat roi = mat.submat(new Rect(upperLeftX, upperLeftY, cropWidth, cropHeight));
        mergeImg(fin, roi);
        release(roi, fin);
        if (regionBoard) {
            drawRectangle(mat, new Scalar(0, 255, 0));
        }
        return mat;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mat filter(Mat mat) throws ModuleException, CvException {
        List<ModuleStatus> moduleStatusList = ((List<ModuleStatus>) MODULE_PROFILE_STATUS_LIST.value())
                .stream()
                .filter(ModuleStatus::isEnable)
                .sorted(Comparator.comparingInt(ModuleStatus::getIndex))
                .collect(Collectors.toList());
        if (moduleStatusList.isEmpty()) {
            throw new ModuleException("");
        }
        if (moduleStatusList.get(0).getModuleType() != ModuleType.CROP) {
            throw new ModuleException("The first module can only be CROP");
        }
        cacheMap.clear();
        Map<String, Object> map;
        for (ModuleStatus moduleStatus : moduleStatusList) {
            map = moduleStatus.getParamMap();
            try {
                switch (moduleStatus.getModuleType()) {
                    case CROP:
                        mat = cropImage(mat,
                                moduleService.cvtInt(map.get("ulx")),
                                moduleService.cvtInt(map.get("uly")),
                                moduleService.cvtInt(map.get("lrx")),
                                moduleService.cvtInt(map.get("lry"))
                        );
                        regionBoard = map.getOrDefault("sr", 0D).equals(1D);
                        break;
                    case CVT_COLOR:
                        cvtColor(mat,
                                moduleService.cvtInt(map.get("cm"))
                        );
                        break;
                    case EQUALIZE_HIST:
                        equalizeHist(mat,
                                moduleService.cvtInt(map.get("ci"))
                        );
                        break;
                    case MAX_CCL:
                        removeLargeArea(mat,
                                moduleService.cvtDouble(map.get("a"))
                        );
                        break;
                    case MIN_CCL:
                        minAreaUtil.setMinArea(moduleService.cvtDouble(map.get("a")));
                        minAreaUtil.removeLessArea(mat);
                        break;
                    case CVT_DEPTH:
                        cvtDepth(mat,
                                moduleService.cvtInt(map.get("d"))
                        );
                        break;
                    case BOX_FILTER:
                        boxFilter(mat,
                                moduleService.cvtInt(map.get("kw")),
                                moduleService.cvtInt(map.get("kh")),
                                moduleService.cvtDouble(map.get("ax")),
                                moduleService.cvtDouble(map.get("ay")),
                                (Boolean) map.get("n"),
                                moduleService.cvtInt(map.get("bt"))
                        );
                        break;
                    case MORPHOLOGY:
                        morphology(mat,
                                moduleService.cvtInt(map.get("ms")),
                                moduleService.cvtInt(map.get("kw")),
                                moduleService.cvtInt(map.get("kh")),
                                moduleService.cvtDouble(map.get("sax")),
                                moduleService.cvtDouble(map.get("say")),
                                moduleService.cvtInt(map.get("mt")),
                                moduleService.cvtDouble(map.get("ax")),
                                moduleService.cvtDouble(map.get("ay")),
                                moduleService.cvtInt(map.get("i")),
                                moduleService.cvtInt(map.get("bt"))
                        );
                        break;
                    case HSV_IN_RANGE:
                        inRange(mat,
                                moduleService.cvtInt(map.get("hmi")),
                                moduleService.cvtInt(map.get("smi")),
                                moduleService.cvtInt(map.get("vmi")),
                                moduleService.cvtInt(map.get("hma")),
                                moduleService.cvtInt(map.get("sma")),
                                moduleService.cvtInt(map.get("vma"))
                        );
                        break;
                    case MEDIAN_BLUR:
                        medianBlur(mat,
                                moduleService.cvtInt(map.get("ks"))
                        );
                        break;
                    case HLS_IN_RANGE:
                        inRange(mat,
                                moduleService.cvtInt(map.get("hmi")),
                                moduleService.cvtInt(map.get("lmi")),
                                moduleService.cvtInt(map.get("smi")),
                                moduleService.cvtInt(map.get("hma")),
                                moduleService.cvtInt(map.get("lma")),
                                moduleService.cvtInt(map.get("sma"))
                        );
                        break;
                    case GAUSSIAN_BLUR:
                        gaussianBlur(mat,
                                moduleService.cvtInt(map.get("kw")),
                                moduleService.cvtInt(map.get("kh")),
                                moduleService.cvtDouble(map.get("sx")),
                                moduleService.cvtDouble(map.get("sy")),
                                moduleService.cvtInt(map.get("bt"))
                        );
                        break;
                    case BILATERAL_FILTER:
                        bilateralFilter(mat,
                                moduleService.cvtInt(map.get("d")),
                                moduleService.cvtDouble(map.get("sc")),
                                moduleService.cvtDouble(map.get("ss")),
                                moduleService.cvtInt(map.get("bt"))
                        );
                        break;
                    case FIXED_BINARIZATION:
                        fixedBinarization(mat,
                                moduleService.cvtDouble(map.get("tt")),
                                moduleService.cvtInt(map.get("mv")),
                                moduleService.cvtInt(map.get("t"))
                        );
                        break;
                    case ADAPTIVE_BINARIZATION:
                        adaptiveBinarization(mat,
                                moduleService.cvtInt(map.get("mv")),
                                moduleService.cvtInt(map.get("am")),
                                moduleService.cvtInt(map.get("tt")),
                                moduleService.cvtInt(map.get("bs")),
                                moduleService.cvtDouble(map.get("c"))
                        );
                        break;
                    case INVERT_BINARIZATION:
                        Core.bitwise_not(mat, mat);
                        break;
                    case ARITHMETIC_OPERATION:
                        arithmetic(mat,
                                moduleService.cvtInt(map.get("ot")),
                                moduleService.cvtInt(map.get("o1i")),
                                moduleService.cvtInt(map.get("o2i")));
                        break;
                }
            } catch (CvException | ModuleException e) {
                throw new ModuleException(String.format(moduleMsg,
                        resourceBundle.getString(joiner.join("module", moduleStatus.getModuleType().toLowerCase())),
                        moduleStatus.toString(), e.getMessage()), e);
            }
            if (moduleStatus.isCache()) {
                cacheMap.put(moduleStatus.getIndex(), mat.clone());
            } else cacheMap.remove(moduleStatus.getIndex());
        }
        minAreaUtil.clearMinArea();
        return mat;
    }

    @Override
    public int countBlackPixel(Mat mat) {
        return (int) mat.total() - Core.countNonZero(mat);
    }

    @Override
    public int countWhitePixel(Mat mat) {
        return Core.countNonZero(mat);
    }

    @Override
    public double psnr(Mat mat1, Mat mat2) {
        return Core.PSNR(mat1, mat2);
    }

    @Deprecated
    public double getPSNR(Mat mat1, Mat mat2) {
        Mat s = new Mat();
        absdiff(mat1, mat2, s);
        cvtColor(s, CV_32F);
        s = s.mul(s);
        Scalar scalar = sumElems(s);
        double sse = scalar.val[0] + scalar.val[1] + scalar.val[2];
        if (sse <= 1e-10) {
            return 0;
        } else {
            double mse = sse / (double) (mat1.channels() * mat1.total());
            return 10.0 * log10((255 * 255) / mse);
        }
    }

    /**
     * From OpenCV
     */
    @Override
    public Scalar meanSSIM(Mat mat1, Mat mat2) {
        double C1 = 6.5025, C2 = 58.5225;
        Mat I1 = new Mat();
        Mat I2 = new Mat();
        mat1.convertTo(I1, CV_32F);
        mat2.convertTo(I2, CV_32F);

        Mat I2_2 = I2.mul(I2);
        Mat I1_2 = I1.mul(I1);
        Mat I1_I2 = I1.mul(I2);

        Mat mu1 = new Mat();
        Mat mu2 = new Mat();
        GaussianBlur(I1, mu1, new Size(11, 11), 1.5);
        GaussianBlur(I2, mu2, new Size(11, 11), 1.5);

        Mat mu1_2 = mu1.mul(mu1);
        Mat mu2_2 = mu2.mul(mu2);
        Mat mu1_mu2 = mu1.mul(mu2);

        Mat sigma1_2 = new Mat();
        Mat sigma2_2 = new Mat();
        Mat sigma12 = new Mat();

        GaussianBlur(I1_2, sigma1_2, new Size(11, 11), 1.5);
        subtract(sigma1_2, mu1_2, sigma1_2);

        GaussianBlur(I2_2, sigma2_2, new Size(11, 11), 1.5);
        subtract(sigma2_2, mu2_2, sigma2_2);

        GaussianBlur(I1_I2, sigma12, new Size(11, 11), 1.5);
        subtract(sigma12, mu1_mu2, sigma12);

        Mat t1 = new Mat();
        Mat t2 = new Mat();
        Mat t3;

        multiply(mu1_mu2, new Scalar(2), t1);
        add(t1, new Scalar(C1), t1);

        multiply(sigma12, new Scalar(2), t2);
        add(t2, new Scalar(C2), t2);

        t3 = t1.mul(t2);

        add(mu1_2, mu2_2, t1);
        add(t1, new Scalar(C1), t1);

        add(sigma1_2, sigma2_2, t2);
        add(t2, new Scalar(C2), t2);

        t1 = t1.mul(t2);

        Mat ssim_map = new Mat();
        divide(t3, t1, ssim_map);
        Scalar scalar = mean(ssim_map);
        release(I1, I2, I2_2, I1_2, I1_I2, mu1, mu2, mu1_2, mu2_2, mu1_mu2,
                sigma1_2, sigma2_2, sigma12, t1, t2, t3, ssim_map);
        return scalar;
    }

    private Mat cropImage(Mat mat, int upperLeftX, int upperLeftY, int lowerRightX, int lowerRightY) throws ModuleException {
        if ((lowerRightX <= upperLeftX) || (lowerRightY <= upperLeftY)) {
            throw new ModuleException("Invalid region, lower(x,y) less than upper(x,y)");
        }
        this.upperLeftX = upperLeftX;
        this.upperLeftY = upperLeftY;
        this.lowerRightX = lowerRightX;
        this.lowerRightY = lowerRightY;
        this.cropWidth = lowerRightX - upperLeftX;
        this.cropHeight = lowerRightY - upperLeftY;
        return mat.submat(new Rect(upperLeftX, upperLeftY, cropWidth, cropHeight));
    }

    private void cvtColor(Mat mat, int code) {
        Imgproc.cvtColor(mat, mat, code);
    }

    private void cvtDepth(Mat mat, int code) {
        mat.convertTo(mat, code);
    }

    private void equalizeHist(Mat mat, int channelIndex) {
        List<Mat> list = new ArrayList<>();
        Core.split(mat, list);
        if (list.size() == 0) {
            return;
        }
        if (channelIndex == -1) {
            for (Mat lMat : list) {
                Imgproc.equalizeHist(lMat, lMat);
            }
        } else {
            if (channelIndex >= list.size()) {
                channelIndex = list.size() - 1;
            }
            Imgproc.equalizeHist(list.get(channelIndex), list.get(channelIndex));
        }
        Core.merge(list, mat);
    }

    private void boxFilter(Mat mat,
                           int kernelWidth,
                           int kernelHeight,
                           double anchorX,
                           double anchorY,
                           boolean isNormalize,
                           int borderType) {
        Imgproc.boxFilter(mat, mat, -1,
                new Size(kernelWidth, kernelHeight),
                new Point(anchorX, anchorY),
                isNormalize,
                borderType);
    }

    private void medianBlur(Mat mat, int kernelSize) {
        Imgproc.medianBlur(mat, mat, kernelSize);
    }

    private void gaussianBlur(Mat mat,
                              int kernelWidth,
                              int kernelHeight,
                              double sigmaX,
                              double sigmaY,
                              int borderType) {
        Imgproc.GaussianBlur(mat, mat, new Size(kernelWidth, kernelHeight), sigmaX, sigmaY, borderType);
    }

    private void fixedBinarization(Mat mat,
                                   double threshold,
                                   int maxValue,
                                   int type) {
        Imgproc.threshold(mat, mat, threshold, maxValue, type);
    }

    private void adaptiveBinarization(Mat mat,
                                      int maxValue,
                                      int adaptiveMethod,
                                      int thresholdType,
                                      int blockSize,
                                      double c) {
        Imgproc.adaptiveThreshold(mat, mat, maxValue, adaptiveMethod, thresholdType, blockSize, c);
    }

    private void morphology(Mat mat,
                            int morphShape,
                            int kernelWidth, int kernelHeight,
                            double pointX, double pointY,
                            int morphologyType,
                            double anchorX, double anchorY,
                            int i,
                            int borderType) {
        Mat kernel = Imgproc.getStructuringElement(morphShape,
                new Size(kernelWidth, kernelHeight),
                new Point(pointX, pointY));
        Imgproc.morphologyEx(mat, mat, morphologyType, kernel,
                new Point(anchorX, anchorY), i, borderType);
    }

    private void bilateralFilter(Mat mat,
                                 int d,
                                 double sigmaColor,
                                 double sigmaSpace,
                                 int borderType) {
        Mat src = mat.clone();
        Imgproc.bilateralFilter(src, mat, d, sigmaColor, sigmaSpace, borderType);
        src.release();
    }

    private void inRange(Mat mat,
                         int low1, int low2, int low3,
                         int up1, int up2, int up3) {
        Core.inRange(mat, new Scalar(low1, low2, low3), new Scalar(up1, up2, up3), mat);
    }

    private void removeLargeArea(Mat mat, double maxArea) {
        List<MatOfPoint> contours = new ArrayList<>();
        List<Rect> rects = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat roi = new Mat();
        Imgproc.findContours(mat, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_NONE);
        for (MatOfPoint matOfPoint : contours) {
            double area = Imgproc.contourArea(matOfPoint);
            if (area > maxArea) {
                rects.add(Imgproc.boundingRect(matOfPoint));
            }
        }
        for (Rect rect : rects) {
            roi = mat.submat(rect);
            byte[] bytes = mat2ByteArrayByMatOfByte(roi);
            for (int i = 0, len = bytes.length; i < len; i++) {
                bytes[i] = 0;
            }
            roi.put(0, 0, bytes);
            Core.addWeighted(roi, 1, Mat.zeros(roi.rows(), roi.cols(), roi.depth()), 0, 0, roi);
        }
        //Core.bitwise_not(mat, mat);
        release(roi, hierarchy);
    }

    /**
     * @param mat    ori mat
     * @param scalar line color
     */
    private void drawRectangle(Mat mat, final Scalar scalar) {
        Imgproc.rectangle(mat,
                new Point(upperLeftX, upperLeftY),
                new Point(lowerRightX, lowerRightY),
                scalar);
    }

    private void arithmetic(Mat mat, int operationType, int index1, int index2) {
        if (!cacheMap.containsKey(index1) || !cacheMap.containsKey(index2)) {
            return;
        }
        switch (operationType) {
            case 0: //ADD
                Core.add(cacheMap.get(index1), cacheMap.get(index2), mat);
                break;
            case 1: //SUBTRACT
                Core.subtract(cacheMap.get(index1), cacheMap.get(index2), mat);
                break;
            case 2: //MULTIPLY
                Core.multiply(cacheMap.get(index1), cacheMap.get(index2), mat);
                break;
            case 3: //DIVIDE
                Core.divide(cacheMap.get(index1), cacheMap.get(index2), mat);
                break;
            case 4: //ABS_DIFF
                Core.absdiff(cacheMap.get(index1), cacheMap.get(index2), mat);
                break;
            case 5: //BITWISE_AND
                Core.bitwise_and(cacheMap.get(index1), cacheMap.get(index2), mat);
                break;
            case 6: //BITWISE_OR
                Core.bitwise_or(cacheMap.get(index1), cacheMap.get(index2), mat);
                break;
            case 7: //BITWISE_XOR
                Core.bitwise_xor(cacheMap.get(index1), cacheMap.get(index2), mat);
                break;
            case 8: //MAX
                Core.max(cacheMap.get(index1), cacheMap.get(index2), mat);
                break;
            case 9: //MIN
                Core.min(cacheMap.get(index1), cacheMap.get(index2), mat);
                break;
        }
    }

    private void mergeImg(Mat mat, Mat roi) {
        int[] code = new int[]{COLOR_GRAY2BGR, COLOR_HSV2BGR, COLOR_HSV2BGR_FULL, COLOR_HLS2BGR, COLOR_HLS2BGR_FULL};
        for (int c : code) {
            try {
                cvtColor(mat, c);
                Core.addWeighted(roi, 0, mat, 1, 0, roi);
                break;
            } catch (CvException ignored) {
            }
        }
    }

    private void release(Mat... mats) {
        Arrays.stream(mats).filter(Objects::nonNull).forEach(Mat::release);
    }

    private static class MinAreaUtil {

        private double minArea;

        public MinAreaUtil() {
            this.minArea = -1D;
        }

        public void clearMinArea() {
            this.minArea = -1D;
        }

        public void setMinArea(double minArea) {
            if (minArea >= 0) {
                return;
            }
            this.minArea = minArea;
        }

        final void removeLessArea(Mat mat) {
            int iw = mat.width();
            int ih = mat.height();
            for (int i = 0; i < ih; i++) {
                for (int j = 0; j < iw; j++) {
                    double[] colors = mat.get(i, j);
                    if (colors[0] < 255.0 && isPixelStart(i, j, mat)) {
                        List<Point> checkPoint = new ArrayList<>();
                        List<Point> checkedPoint = new ArrayList<>();
                        checkPoint.add(new Point(i, j));
                        ergodic(mat, checkPoint, checkedPoint);
                    }
                }
            }
        }

        private boolean isPixelStart(int i, int j, Mat srcImage) {
            int before = j - 1;
            int top = i - 1;
            return (before < 0 || srcImage.get(i, before)[0] == 255.0) && (top < 0 || srcImage.get(top, j)[0] == 255.0);
        }

        private void ergodic(Mat mat, List<Point> sp, List<Point> lp) {
            for (int k = 0; k < sp.size(); k++) {
                check((int) sp.get(k).x, (int) sp.get(k).y, sp, lp, mat);
                if (sp.size() >= minArea) {
                    lp.clear();
                    break;
                }
            }
            for (Point point : lp) {
                mat.put((int) point.x, (int) point.y, 255.0);
            }
        }

        @SuppressWarnings("SuspiciousNameCombination")
        private void check(int i, int j, List<Point> sp, List<Point> lp, Mat mat) {
            int before = j - 1;
            int after = j + 1;
            int top = i - 1;
            int bottom = i + 1;
            if (before >= 0 && mat.get(i, before)[0] == 0.0) {
                if (isPixelNotExist(i, before, lp) && isPixelNotFound(i, before, sp)) {
                    sp.add(new Point(i, before));
                }
            }
            if (after < mat.width() && mat.get(i, after)[0] == 0.0) {
                if (isPixelNotExist(i, after, lp) && isPixelNotFound(i, after, sp)) {
                    sp.add(new Point(i, after));
                }
            }
            if (top >= 0 && mat.get(top, j)[0] == 0.0) {
                if (isPixelNotExist(top, j, lp) && isPixelNotFound(top, j, sp)) {
                    sp.add(new Point(top, j));
                }
            }
            if (bottom < mat.height() && mat.get(bottom, j)[0] == 0.0) {
                if (isPixelNotExist(bottom, j, lp) && isPixelNotFound(bottom, j, sp)) {
                    sp.add(new Point(bottom, j));
                }
            }
            lp.add(new Point(i, j));
        }

        private boolean isPixelNotExist(int i, int j, List<Point> lp) {
            if (lp.size() == 0) {
                return true;
            }
            for (Point point : lp) {
                if (point.x == i && point.y == j) {
                    return false;
                }
            }
            return true;
        }

        private boolean isPixelNotFound(int i, int j, List<Point> sp) {
            if (sp.size() == 0) {
                return true;
            }
            for (Point point : sp) {
                if (point.x == i && point.y == j) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class OperationType {

        public static final Integer ADD = 0;
        public static final Integer SUBTRACT = 1;
        public static final Integer MULTIPLY = 2;
        public static final Integer DIVIDE = 3;
        public static final Integer ABS_DIFF = 4;
        public static final Integer BITWISE_AND = 5;
        public static final Integer BITWISE_OR = 6;
        public static final Integer BITWISE_XOR = 7;
        public static final Integer MAX = 8;
        public static final Integer MIN = 9;

    }
}
