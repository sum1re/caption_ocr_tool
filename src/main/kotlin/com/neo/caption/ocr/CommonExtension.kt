package com.neo.caption.ocr

import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.abs

fun Mat.toByteArray() = ByteArray(this.calcTotal().toInt()).also { this.get(0, 0, it) }

fun Mat.toEncodeByteArray(
    ext: String = ".webp",
    extQuality: Int = Imgcodecs.IMWRITE_WEBP_QUALITY,
    quality: Int = 80
): ByteArray =
    MatOfByte().also { Imgcodecs.imencode(ext, this, it, MatOfInt(extQuality, quality)) }.toArray()

fun Mat.calcWhitePixel() = Core.countNonZero(this)

fun Mat.calcBlackPixel() = this.total().toInt() - this.calcWhitePixel()

fun Mat.calcArea() = Imgproc.contourArea(this)

fun Mat.calcPSNR(dst: Mat) = Core.PSNR(this, dst)

fun Mat.calcSSIM(dst: Mat): Double {
    val kernelSize = Size(11.0, 11.0)
    val c1 = 6.5025
    val c2 = 58.5225
    val i1 = this.clone().apply { cvtType(CvType.CV_32F) }
    val i2 = dst.clone().apply { this.cvtType(CvType.CV_32F) }
    val i1Mul1 = i1.mul(i1)
    val i2Mul2 = i2.mul(i2)
    val i1MulI2 = i1.mul(i2)
    val mu1 = i1.clone().apply { this.gaussianBlur(kernelSize, 1.5, 1.5, Core.BORDER_DEFAULT) }
    val mu2 = i2.clone().apply { this.gaussianBlur(kernelSize, 1.5, 1.5, Core.BORDER_DEFAULT) }
    i1.release()
    i2.release()

    val mu1MulMu1 = mu1.mul(mu1)
    val mu2MulMu2 = mu2.mul(mu2)
    val mu1MulMu2 = mu1.mul(mu2)
    mu1.release()
    mu2.release()

    val sigmaI1Mul1 = i1Mul1.clone().apply { this.gaussianBlur(kernelSize, 1.5, 1.5, Core.BORDER_DEFAULT) }
    sigmaI1Mul1.subtract(mu1MulMu1)
    val sigmaI2Mul2 = i2Mul2.clone().apply { this.gaussianBlur(kernelSize, 1.5, 1.5, Core.BORDER_DEFAULT) }
    sigmaI2Mul2.subtract(mu2MulMu2)
    val sigmaI1MulI2 = i1MulI2.clone().apply { this.gaussianBlur(kernelSize, 1.5, 1.5, Core.BORDER_DEFAULT) }
    sigmaI1MulI2.subtract(mu1MulMu2)
    i1Mul1.release()
    i2Mul2.release()
    i1MulI2.release()

    val t1 = Mat().apply {
        Core.multiply(mu1MulMu2, Scalar(2.0), this)
        Core.add(this, Scalar(c1), this)
    }
    val t2 = Mat().apply {
        Core.multiply(sigmaI1MulI2, Scalar(2.0), this)
        Core.add(this, Scalar(c2), this)
    }
    val t3 = t1.mul(t2)
    Core.add(mu1MulMu1, mu2MulMu2, t1)
    Core.add(t1, Scalar(c1), t1)
    Core.add(sigmaI1Mul1, sigmaI2Mul2, t2)
    Core.add(t2, Scalar(c2), t2)
    val result = t3.mul(t2).use { t4 ->
        Mat().use {
            Core.divide(t3, t4, it)
            Core.mean(it).`val`[0]
        }
    }
    mu1MulMu1.release()
    mu2MulMu2.release()
    mu1MulMu2.release()
    sigmaI1Mul1.release()
    sigmaI2Mul2.release()
    sigmaI1MulI2.release()
    t1.release()
    t2.release()
    t3.release()
    return result
}

fun Mat.calcTotal(): Long = this.total() * this.channels()

fun Mat.getPixelColor(x: Int, y: Int): Map<String, Int> {
    val map = mutableMapOf("#x" to x, "#y" to y)
    val bgr = this.get(x, y)
    map["#rgB"] = bgr[0].toInt()
    map["#rGb"] = bgr[1].toInt()
    map["#Rgb"] = bgr[2].toInt()
    Mat(1, 1, CvType.CV_8UC3, Scalar(bgr)).use { pixel ->
        pixel.clone().use {
            it.cvtColor(Imgproc.COLOR_BGR2HSV)
            val hsv = it.get(0, 0)
            map["#Hsv"] = hsv[0].toInt()
            map["#hSv"] = hsv[1].toInt()
            map["#hsV"] = hsv[2].toInt()
        }
        pixel.clone().use {
            it.cvtColor(Imgproc.COLOR_BGR2HLS)
            val hls = it.get(0, 0)
            map["#Hls"] = hls[0].toInt()
            map["#hLs"] = hls[1].toInt()
            map["#hlS"] = hls[2].toInt()
        }
    }
    return map.toMap()
}

fun Mat.add(dst: Mat) = Core.add(this, dst, this)

fun Mat.addWeighted(alpha: Double, dst: Mat, beta: Double, gamma: Double) =
    Core.addWeighted(this, alpha, dst, beta, gamma, this)

fun Mat.subtract(dst: Mat) = Core.subtract(this, dst, this)

fun Mat.multiply(dst: Mat) = Core.multiply(this, dst, this)

fun Mat.divide(dst: Mat) = Core.divide(this, dst, this)

fun Mat.absDiff(dst: Mat) = Core.absdiff(this, dst, this)

fun Mat.bitwiseNOT(dst: Mat) = Core.bitwise_not(this, dst)

fun Mat.bitwiseAND(dst: Mat) = Core.bitwise_and(this, dst, this)

fun Mat.bitwiseOR(dst: Mat) = Core.bitwise_or(this, dst, this)

fun Mat.bitwiseXOR(dst: Mat) = Core.bitwise_xor(this, dst, this)

fun Mat.max(dst: Mat) = Core.max(this, dst, this)

fun Mat.min(dst: Mat) = Core.min(this, dst, this)

fun Mat.adaptiveBinarization(adaptiveMethod: Int, thresholdType: Int, blockSize: Int, constant: Double) =
    Imgproc.adaptiveThreshold(this, this, 255.0, adaptiveMethod, thresholdType, blockSize, constant)

fun Mat.fixedBinarization(threshold: Double, type: Int) =
    Imgproc.threshold(this, this, threshold, 255.0, type)

fun Mat.bilateralFilter(diameter: Int, sigmaColor: Double, sigmaSpace: Double, borderType: Int) =
    this.clone().use { Imgproc.bilateralFilter(it, this, diameter, sigmaColor, sigmaSpace, borderType) }

fun Mat.boxFilter(kernelSize: Size, anchorPoint: Point, normalize: Boolean, borderType: Int) =
    Imgproc.boxFilter(this, this, -1, kernelSize, anchorPoint, normalize, borderType)

fun Mat.crop(upperLeftX: Int, upperLeftY: Int, lowerRightX: Int, lowerRightY: Int): Mat =
    this.submat(upperLeftY, lowerRightY, upperLeftX, lowerRightX)

fun Mat.checkRange(upperLeftX: Int, upperLeftY: Int, lowerRightX: Int, lowerRightY: Int) {
    if (lowerRightX <= upperLeftX || abs(lowerRightX - upperLeftX) > this.cols()) {
        throw CvException("lowerRightX(${lowerRightX}) <= upperLeftX(${upperLeftX}) or range less than ${this.cols()}")
    }
    if (lowerRightY <= upperLeftY || abs(lowerRightY - upperLeftY) > this.rows()) {
        throw CvException("lowerRightY(${lowerRightY}) <= upperLeftY(${upperLeftY}) or range less than ${this.rows()}")
    }
}

fun Mat.cvtColor(colorType: Int) = Imgproc.cvtColor(this, this, colorType)

fun Mat.cvtType(type: Int) = this.convertTo(this, type)

fun Mat.equalizeHist(channelIndex: Int) {
    val list = mutableListOf<Mat>().also { Core.split(this, it) }
    if (list.isEmpty() || channelIndex >= list.size) {
        throw CvException("Failed to split mat or the channelIndex is greater than the number of mat channel")
    }
    if (channelIndex == -1) {
        list.forEach { Imgproc.equalizeHist(it, it) }
    } else {
        Imgproc.equalizeHist(list[channelIndex], list[channelIndex])
    }
    Core.merge(list, this)
}

fun Mat.findContours(contours: List<MatOfPoint>, hierarchy: Mat, mode: Int, method: Int) =
    Imgproc.findContours(this, contours, hierarchy, mode, method)

fun Mat.gaussianBlur(kernelSize: Size, sigmaX: Double, sigmaY: Double, borderType: Int) =
    Imgproc.GaussianBlur(this, this, kernelSize, sigmaX, sigmaY, borderType)

fun Mat.medianBlur(kernelSize: Int) =
    Imgproc.medianBlur(this, this, kernelSize)

fun Mat.inRange(low1: Double, low2: Double, low3: Double, up1: Double, up2: Double, up3: Double) =
    Core.inRange(this, Scalar(low1, low2, low3), Scalar(up1, up2, up3), this)

fun Mat.rectangle(upperLeftX: Double, upperLeftY: Double, lowerRightX: Double, lowerRightY: Double, scalar: Scalar) =
    Imgproc.rectangle(this, Point(upperLeftX, upperLeftY), Point(lowerRightX, lowerRightY), scalar)

fun Mat.morphology(
    morphShape: Int,
    kernelSize: Size,
    elementAnchorPoint: Point,
    morphologyType: Int,
    anchorPoint: Point,
    i: Int,
    borderType: Int
) = Imgproc.getStructuringElement(morphShape, kernelSize, elementAnchorPoint).let {
    Imgproc.morphologyEx(this, this, morphologyType, it, anchorPoint, i, borderType)
}

fun Mat.removeLargeArea(maxArea: Double) {
    val contours = mutableListOf<MatOfPoint>()
    Mat().use { hierarchy ->
        this.findContours(contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE)
        contours.filter { it.calcArea() > maxArea }
            .map { Imgproc.boundingRect(it) }
            .map { this.submat(it) }
            .forEach {
                it.use { roi ->
                    val byteArray = roi.toByteArray()
                    for (i in byteArray.indices) {
                        byteArray[i] = 0
                    }
                    roi.put(0, 0, byteArray)
                    roi.addWeighted(1.0, Mat.zeros(it.rows(), roi.cols(), roi.depth()), 0.0, 0.0)
                }
            }
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <R> Mat.use(block: (Mat) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    try {
        return block(this)
    } catch (e: Throwable) {
        throw e
    } finally {
        try {
            this.release()
        } catch (closeException: Throwable) {
            // ignored here
        }
    }
}
