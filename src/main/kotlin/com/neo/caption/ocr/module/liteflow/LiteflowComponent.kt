package com.neo.caption.ocr.module.liteflow

import com.neo.caption.ocr.module.cv.AdaptiveBinarization
import com.neo.caption.ocr.module.cv.BilateralFilter
import com.neo.caption.ocr.module.cv.BoxFilter
import com.neo.caption.ocr.module.cv.CropRange
import com.neo.caption.ocr.module.cv.FixedBinarization
import com.neo.caption.ocr.module.cv.GaussianBlur
import com.neo.caption.ocr.module.cv.HLSRange
import com.neo.caption.ocr.module.cv.HSVRange
import com.neo.caption.ocr.module.cv.Morphology
import com.neo.caption.ocr.module.cv.SingleIntParam
import com.neo.caption.ocr.module.cv.adaptiveBinarization
import com.neo.caption.ocr.module.cv.bilateralFilter
import com.neo.caption.ocr.module.cv.boxFilter
import com.neo.caption.ocr.module.cv.crop
import com.neo.caption.ocr.module.cv.cvtColor
import com.neo.caption.ocr.module.cv.cvtType
import com.neo.caption.ocr.module.cv.equalizeHist
import com.neo.caption.ocr.module.cv.fixedBinarization
import com.neo.caption.ocr.module.cv.gaussianBlur
import com.neo.caption.ocr.module.cv.inRange
import com.neo.caption.ocr.module.cv.medianBlur
import com.neo.caption.ocr.module.cv.morphology
import com.yomahub.liteflow.annotation.LiteflowComponent
import com.yomahub.liteflow.core.NodeBreakComponent
import com.yomahub.liteflow.core.NodeComponent
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture
import kotlin.reflect.KClass

sealed interface BaseContext

data class CvContext(
    val matStack: MutableList<Mat> // store history
) : BaseContext {
    fun produce(action: Mat.() -> Any) {
        val copy = matStack.last().clone()!!
        when (val result = action(copy)) {
            is Mat -> matStack.add(result) // action returns Mat, add result to stack.
            is Unit -> matStack.add(copy) // action returns Unit, add copy to stack.
        }
    }

    fun release() {
        matStack.forEach { it.release() }
        matStack.clear()
    }
}

data class ProjectContext(
    val projectId: String,
    val videoCaption: VideoCapture,
    val filteredMat: MutableList<Mat>,
) : BaseContext

@LiteflowComponent
class BreakNode : NodeBreakComponent() {
    override fun processBreak() = false
}

abstract class CvComponent<T : Any, R : Any> : NodeComponent() {
    abstract val kClass: KClass<T>
    lateinit var componentParam: T
    abstract val action: Mat.() -> R

    override fun isAccess(): Boolean {
        componentParam = this.getCmpData(kClass.java) ?: return false
        return true
    }

    override fun process() {
        context<CvContext>().produce(action)
    }
}

@LiteflowComponent("crop")
class CropComponent : CvComponent<CropRange, Mat>() {
    override val kClass = CropRange::class
    override val action: Mat.() -> Mat = { this.crop(componentParam) }
}

@LiteflowComponent("morphology")
class MorphologyComponent : CvComponent<Morphology, Unit>() {
    override val kClass = Morphology::class
    override val action: Mat.() -> Unit = { this.morphology(componentParam) }
}

@LiteflowComponent("adaptiveBinarization")
class AdaptiveBinarizationComponent : CvComponent<AdaptiveBinarization, Unit>() {
    override val kClass = AdaptiveBinarization::class
    override val action: Mat.() -> Unit = { this.adaptiveBinarization(componentParam) }
}

@LiteflowComponent("fixedBinarization")
class FixedBinarizationComponent : CvComponent<FixedBinarization, Unit>() {
    override val kClass = FixedBinarization::class
    override val action: Mat.() -> Unit = { this.fixedBinarization(componentParam) }
}

@LiteflowComponent("bilateralFilter")
class BilateralFilterComponent : CvComponent<BilateralFilter, Unit>() {
    override val kClass = BilateralFilter::class
    override val action: Mat.() -> Unit = { this.bilateralFilter(componentParam) }
}

@LiteflowComponent("boxFilter")
class BoxFilterComponent : CvComponent<BoxFilter, Unit>() {
    override val kClass = BoxFilter::class
    override val action: Mat.() -> Unit = { this.boxFilter(componentParam) }
}

@LiteflowComponent("gaussianBlur")
class GaussianBlurComponent : CvComponent<GaussianBlur, Unit>() {
    override val kClass = GaussianBlur::class
    override val action: Mat.() -> Unit = { this.gaussianBlur(componentParam) }
}

@LiteflowComponent("medianBlur")
class MedianBlurComponent : CvComponent<SingleIntParam, Unit>() {
    override val kClass = SingleIntParam::class
    override val action: Mat.() -> Unit = { this.medianBlur(componentParam) }
}

@LiteflowComponent("hlsRange")
class HLSRangeComponent : CvComponent<HLSRange, Unit>() {
    override val kClass = HLSRange::class
    override val action: Mat.() -> Unit = {
        this.inRange(
            componentParam.hueMin,
            componentParam.lightnessMin,
            componentParam.saturationMin,
            componentParam.hueMax,
            componentParam.lightnessMax,
            componentParam.saturationMax
        )
    }
}

@LiteflowComponent("hsvRange")
class HSVRangeComponent : CvComponent<HSVRange, Unit>() {
    override val kClass = HSVRange::class
    override val action: Mat.() -> Unit = {
        this.inRange(
            componentParam.hueMin,
            componentParam.saturationMin,
            componentParam.valueMin,
            componentParam.hueMax,
            componentParam.saturationMax,
            componentParam.valueMax
        )
    }
}

@LiteflowComponent("convertColor")
class ConvertColorComponent : CvComponent<SingleIntParam, Unit>() {
    override val kClass = SingleIntParam::class
    override val action: Mat.() -> Unit = { this.cvtColor(componentParam) }
}

@LiteflowComponent("convertDepth")
class ConvertDepthComponent : CvComponent<SingleIntParam, Unit>() {
    override val kClass = SingleIntParam::class
    override val action: Mat.() -> Unit = { this.cvtType(componentParam) }
}

@LiteflowComponent("equalize")
class EqualizeComponent : CvComponent<SingleIntParam, Unit>() {
    override val kClass = SingleIntParam::class
    override val action: Mat.() -> Unit = { this.equalizeHist(componentParam) }
}

// TODO: add flow for arithmetic operation
