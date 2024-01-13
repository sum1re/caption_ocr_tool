package com.neo.caption.ocr.module.cv

import com.yomahub.liteflow.annotation.LiteflowComponent
import com.yomahub.liteflow.core.NodeComponent
import org.opencv.core.Mat
import kotlin.reflect.KClass

data class CvContext(
    val originMat: Mat, // initial by flow executor
    var nextMat: Mat = originMat.clone(), // consume by each NodeComponent,
    val matHistory: MutableList<Mat> = mutableListOf(originMat.clone()) // store history
) {
    fun produce(action: Mat.() -> Any) {
        when (val result = action(nextMat.clone())) {
            is Mat -> nextMat = result // action returns Mat, just set nextMat to it
            is Unit -> {} // action returns Unit, meaning that the operation occurs directly on nextMat without manipulation.
        }
        matHistory.add(nextMat.clone())
    }

    fun release() {
        originMat.release()
        nextMat.release()
        matHistory.forEach { it.release() }
        matHistory.clear()
    }
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
        context().produce(action)
    }

    override fun afterProcess() {
        super.afterProcess()
        context().release()
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

private fun NodeComponent.context(): CvContext = this.getContextBean(CvContext::class.java)
