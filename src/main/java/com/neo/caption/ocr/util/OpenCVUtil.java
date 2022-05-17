package com.neo.caption.ocr.util;

import com.neo.caption.ocr.util.opencv.CvtColor;
import com.neo.caption.ocr.util.opencv.CvtType;
import com.neo.caption.ocr.util.opencv.GaussianBlur;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

@Slf4j
public class OpenCVUtil {

    @NotNull
    @Contract(value = " -> new", pure = true)
    public static Point centerPoint() {
        return new Point(-1, -1);
    }

    @NotNull
    public static byte[] mat2ByteArrayByGet(@NotNull Mat mat) {
        var channels = (int) mat.total() * mat.channels();
        var bytes = new byte[channels];
        mat.get(0, 0, bytes);
        return bytes;
    }

    public static byte[] mat2ByteArrayByMatOfByte(@NotNull Mat mat) {
        var matOfByte = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, matOfByte);
        return matOfByte.toArray();
    }

    @NotNull
    public static Map<String, Integer> getPixelColor(@NotNull Mat mat, int x, int y) {
        var pixelColorMap = new HashMap<String, Integer>(12);
        pixelColorMap.put("#x", x);
        pixelColorMap.put("#y", y);
        var bgr = mat.get(y, x);
        pixelColorMap.put("#rgB", (int) bgr[0]);
        pixelColorMap.put("#rGb", (int) bgr[1]);
        pixelColorMap.put("#Rgb", (int) bgr[2]);

        var hsvCvtColor = new CvtColor(Imgproc.COLOR_BGR2HSV);
        var hlsCvtColor = new CvtColor(Imgproc.COLOR_BGR2HLS);

        var pixel = new Mat(1, 1, CvType.CV_8UC3, new Scalar(bgr));
        var hsvPixel = hsvCvtColor.cloneProcess(pixel);
        var hlsPixel = hlsCvtColor.cloneProcess(pixel);

        var hsv = hsvPixel.get(0, 0);
        var hls = hlsPixel.get(0, 0);
        pixelColorMap.put("#Hsv", (int) hsv[0]);
        pixelColorMap.put("#hSv", (int) hsv[1]);
        pixelColorMap.put("#hsV", (int) hsv[2]);
        pixelColorMap.put("#Hls", (int) hls[0]);
        pixelColorMap.put("#hLs", (int) hls[1]);
        pixelColorMap.put("#hlS", (int) hls[2]);
        release(pixel, hsvPixel, hlsPixel);
        return pixelColorMap;
    }

    public static int countBlackPixel(@NotNull Mat mat) {
        return (int) mat.total() - Core.countNonZero(mat);
    }

    public static int countWhitePixel(@NotNull Mat mat) {
        return Core.countNonZero(mat);
    }

    public static double psnr(Mat mat1, Mat mat2) {
        return Core.PSNR(mat1, mat2);
    }

    /**
     * From OpenCV
     */
    public static double ssim(Mat mat1, Mat mat2) {
        var cvtType = new CvtType(CvType.CV_32F);
        var gaussianBlurKernel = new Size(11, 11);
        var gaussianBlur = new GaussianBlur(gaussianBlurKernel, 1.5, 1.5, Core.BORDER_DEFAULT);

        var C1 = 6.5025D;
        var C2 = 58.5225D;
        var I1 = cvtType.cloneProcess(mat1);
        var I2 = cvtType.cloneProcess(mat2);

        var I2_2 = I2.mul(I2);
        var I1_2 = I1.mul(I1);
        var I1_I2 = I1.mul(I2);

        var mu1 = gaussianBlur.cloneProcess(I1);
        var mu2 = gaussianBlur.cloneProcess(I2);

        var mu1_2 = mu1.mul(mu1);
        var mu2_2 = mu2.mul(mu2);
        var mu1_mu2 = mu1.mul(mu2);

        var sigma1_2 = gaussianBlur.cloneProcess(I1_2);
        Core.subtract(sigma1_2, mu1_2, sigma1_2);
        var sigma2_2 = gaussianBlur.cloneProcess(I2_2);
        Core.subtract(sigma2_2, mu2_2, sigma2_2);
        var sigma12 = gaussianBlur.cloneProcess(I1_I2);
        Core.subtract(sigma12, mu1_mu2, sigma12);

        Mat t1 = new Mat();
        Mat t2 = new Mat();
        Mat t3;

        Core.multiply(mu1_mu2, new Scalar(2), t1);
        Core.add(t1, new Scalar(C1), t1);

        Core.multiply(sigma12, new Scalar(2), t2);
        Core.add(t2, new Scalar(C2), t2);

        t3 = t1.mul(t2);

        Core.add(mu1_2, mu2_2, t1);
        Core.add(t1, new Scalar(C1), t1);

        Core.add(sigma1_2, sigma2_2, t2);
        Core.add(t2, new Scalar(C2), t2);

        t1 = t1.mul(t2);

        var ssim_map = new Mat();
        Core.divide(t3, t1, ssim_map);
        var scalar = Core.mean(ssim_map);
        release(I1, I2, I2_2, I1_2, I1_I2, mu1, mu2, mu1_2, mu2_2, mu1_mu2,
                sigma1_2, sigma2_2, sigma12, t1, t2, t3, ssim_map);
        return scalar.val[0];
    }

    /**
     * Force to deallocate the matrix.
     *
     * @param mats Mat list, if the mat is null, it will be filtered out.
     */
    public static void release(Mat... mats) {
        Arrays.stream(mats)
                .filter(Objects::nonNull)
                .forEach(Mat::release);
    }

}
