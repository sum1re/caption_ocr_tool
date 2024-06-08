package com.neo.caption.ocr.module.cv

import com.neo.caption.ocr.domain.BaseData
import com.neo.caption.ocr.domain.BaseDto
import org.opencv.core.Point
import org.opencv.core.Size
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

data class NumberPair<T>(val first: T, val second: T)

private fun NumberPair<Double>.toSize() = Size(this.first, this.second)
private fun NumberPair<Double>.toPoint() = Point(this.first, this.second)

data class CropRange(
    val upperLeftX: Int,
    val upperLeftY: Int,
    val lowerRightX: Int,
    val lowerRightY: Int
) : BaseData

data class CropRangeDto(
    val upperLeft: NumberPair<Int>,
    val lowerRight: NumberPair<Int>,
) : BaseDto

data class Morphology(
    val morphType: Int,
    val morphShape: Int,
    val kernel: Size,
    val shapeAnchor: Point,
    val morphAnchor: Point,
    val iteration: Int,
    val border: Int
) : BaseData

data class MorphologyDto(
    val morphType: MorphType,
    val morphShape: MorphShape,
    val kernel: NumberPair<Double>,
    val shapeAnchor: NumberPair<Double>,
    val morphAnchor: NumberPair<Double>,
    val iteration: Int,
    val border: Border,
) : BaseDto

data class AdaptiveBinarization(
    val maxValue: Double,
    val adaptiveMethod: Int,
    val thresholdType: Int,
    val blockSize: Int,
    val constant: Double,
) : BaseData

data class AdaptiveBinarizationDto(
    val maxValue: Double,
    val adaptiveMethod: AdaptiveMethod,
    val thresholdType: ThresholdType,
    val blockSize: Int,
    val constant: Double,
) : BaseDto

data class FixedBinarization(
    val maxValue: Double,
    val thresholdValue: Double,
    val thresholdType: Int
) : BaseData

data class FixedBinarizationDto(
    val maxValue: Double,
    val thresholdValue: Double,
    val thresholdType: ThresholdType
) : BaseDto

data class BilateralFilter(
    val diameter: Int,
    val sigmaColor: Double,
    val sigmaSpace: Double,
    val border: Int
) : BaseData

data class BilateralFilterDto(
    val diameter: Int,
    val sigmaColor: Double,
    val sigmaSpace: Double,
    val border: Border
) : BaseDto

data class BoxFilter(
    val kernel: Size,
    val anchor: Point,
    val normalize: Boolean,
    val border: Int
) : BaseData

data class BoxFilterDto(
    val kernel: NumberPair<Double>,
    val anchor: NumberPair<Double>,
    val normalize: Boolean,
    val border: Border
) : BaseDto

data class GaussianBlur(
    val kernel: Size,
    val sigmaX: Double,
    val sigmaY: Double,
    val border: Int
) : BaseData

data class GaussianBlurDto(
    val kernel: NumberPair<Double>,
    val sigma: NumberPair<Double>,
    val border: Border
) : BaseDto

data class MedianBlurDto(
    val kernelSize: Int
) : BaseDto

data class HLSRange(
    val mask: Boolean,
    val hueMin: Double,
    val hueMax: Double,
    val lightnessMin: Double,
    val lightnessMax: Double,
    val saturationMin: Double,
    val saturationMax: Double,
) : BaseData

data class HLSRangeDto(
    val mask: Boolean,
    val hue: NumberPair<Int>,
    val lightness: NumberPair<Int>,
    val saturation: NumberPair<Int>
) : BaseDto

data class HSVRange(
    val hueMin: Double,
    val hueMax: Double,
    val saturationMin: Double,
    val saturationMax: Double,
    val valueMin: Double,
    val valueMax: Double,
) : BaseData

data class HSVRangeDto(
    val hue: NumberPair<Int>,
    val saturation: NumberPair<Int>,
    val value: NumberPair<Int>
) : BaseDto

data class SingleIntParam(
    val i: Int
) : BaseData

data class ConvertColorDto(
    val colorType: ColorType
) : BaseDto

data class ConvertDepthDto(
    val depth: Depth
) : BaseDto

data class EqualizeHistDto(
    val index: Int
) : BaseDto

// All converter written by Gemini

@Component
class CropRangeConverter : Converter<CropRangeDto, CropRange> {
    override fun convert(source: CropRangeDto) = CropRange(
        upperLeftX = source.upperLeft.first,
        upperLeftY = source.upperLeft.second,
        lowerRightX = source.lowerRight.first,
        lowerRightY = source.lowerRight.second,
    )
}

@Component
class MorphologyConverter : Converter<MorphologyDto, Morphology> {
    override fun convert(source: MorphologyDto) = Morphology(
        morphType = source.morphType.value, // Extract value from enum
        morphShape = source.morphShape.value, // Extract value from enum
        kernel = source.kernel.toSize(),
        shapeAnchor = source.shapeAnchor.toPoint(),
        morphAnchor = source.morphAnchor.toPoint(),
        iteration = source.iteration,
        border = source.border.value // Extract value from enum
    )
}

@Component
class AdaptiveBinarizationConverter : Converter<AdaptiveBinarizationDto, AdaptiveBinarization> {
    override fun convert(source: AdaptiveBinarizationDto) = AdaptiveBinarization(
        maxValue = source.maxValue,
        adaptiveMethod = source.adaptiveMethod.value, // Extract value from enum
        thresholdType = source.thresholdType.value, // Extract value from enum
        blockSize = source.blockSize,
        constant = source.constant,
    )
}

@Component
class FixedBinarizationConverter : Converter<FixedBinarizationDto, FixedBinarization> {
    override fun convert(source: FixedBinarizationDto) = FixedBinarization(
        maxValue = source.maxValue,
        thresholdValue = source.thresholdValue,
        thresholdType = source.thresholdType.value, // Extract value from enum
    )
}

@Component
class BilateralFilterConverter : Converter<BilateralFilterDto, BilateralFilter> {
    override fun convert(source: BilateralFilterDto) = BilateralFilter(
        diameter = source.diameter,
        sigmaColor = source.sigmaColor,
        sigmaSpace = source.sigmaSpace,
        border = source.border.value, // Extract value from enum
    )
}

@Component
class BoxFilterConverter : Converter<BoxFilterDto, BoxFilter> {
    override fun convert(source: BoxFilterDto) = BoxFilter(
        kernel = source.kernel.toSize(),
        anchor = source.anchor.toPoint(),
        normalize = source.normalize,
        border = source.border.value, // Extract value from enum
    )
}

@Component
class GaussianBlurConverter : Converter<GaussianBlurDto, GaussianBlur> {
    override fun convert(source: GaussianBlurDto) = GaussianBlur(
        kernel = source.kernel.toSize(),
        sigmaX = source.sigma.first,
        sigmaY = source.sigma.second,
        border = source.border.value, // Extract value from enum
    )
}

@Component
class MedianBlurConverter : Converter<MedianBlurDto, SingleIntParam> {
    override fun convert(source: MedianBlurDto) = SingleIntParam(source.kernelSize)
}

@Component
class HLSRangeConverter : Converter<HLSRangeDto, HLSRange> {
    override fun convert(source: HLSRangeDto) = HLSRange(
        mask = source.mask,
        hueMin = source.hue.first.toDouble(), // Convert from NumberPair<Int>
        hueMax = source.hue.second.toDouble(), // Convert from NumberPair<Int>
        lightnessMin = source.lightness.first.toDouble(), // Convert from NumberPair<Int>
        lightnessMax = source.lightness.second.toDouble(), // Convert from NumberPair<Int>
        saturationMin = source.saturation.first.toDouble(), // Convert from NumberPair<Int>
        saturationMax = source.saturation.second.toDouble(), // Convert from NumberPair<Int>
    )
}

@Component
class HSVRangeConverter : Converter<HSVRangeDto, HSVRange> {
    override fun convert(source: HSVRangeDto) = HSVRange(
        hueMin = source.hue.first.toDouble(), // Convert from NumberPair<Int>
        hueMax = source.hue.second.toDouble(), // Convert from NumberPair<Int>
        saturationMin = source.saturation.first.toDouble(), // Convert from NumberPair<Int>
        saturationMax = source.saturation.second.toDouble(), // Convert from NumberPair<Int>
        valueMin = source.value.first.toDouble(), // Convert from NumberPair<Int>
        valueMax = source.value.second.toDouble(), // Convert from NumberPair<Int>
    )
}

@Component
class ColorConverter : Converter<ConvertColorDto, SingleIntParam> {
    override fun convert(source: ConvertColorDto) = SingleIntParam(source.colorType.value) // Extract value from enum
}

@Component
class DepthConverter : Converter<ConvertDepthDto, SingleIntParam> {
    override fun convert(source: ConvertDepthDto) = SingleIntParam(source.depth.value) // Extract value from enum
}

@Component
class EqualizeHistConverter : Converter<EqualizeHistDto, SingleIntParam> {
    override fun convert(source: EqualizeHistDto) = SingleIntParam(source.index)
}
