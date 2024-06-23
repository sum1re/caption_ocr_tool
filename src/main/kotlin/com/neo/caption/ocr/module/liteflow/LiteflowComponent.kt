package com.neo.caption.ocr.module.liteflow

import com.neo.caption.ocr.common.ErrorCodeEnum
import com.neo.caption.ocr.common.throwLiteFlowException
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
import com.neo.caption.ocr.module.file.FileService
import com.neo.caption.ocr.module.project.ProjectService
import com.yomahub.liteflow.annotation.LiteflowComponent
import com.yomahub.liteflow.core.NodeBreakComponent
import com.yomahub.liteflow.core.NodeComponent
import com.yomahub.liteflow.core.NodeIfComponent
import com.yomahub.liteflow.core.NodeWhileComponent
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.Videoio
import kotlin.io.path.absolutePathString
import kotlin.reflect.KClass

/**
 * NodeComponent class convention:
 *
 * Name of NodeComponent should start with Liteflow
 * followed by the component id.
 *
 *  ```
 *  @LiteflowComponent("actionA")
 *  class LiteflowActionA : NodeComponent()
 *
 *  @LiteflowComponent("switchEnum")
 *  class LiteflowSwitchEnum : NodeSwitchComponent()
 *  ```
 */

}

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

@LiteflowComponent("startProject")
class StartProjectComponent(
    private val fileService: FileService
) : NodeComponent() {
    override fun process() {
        val context = context<ProjectContext>()
        val file = fileService.findVideoFile(context.projectId).absolutePathString()
        context.videoCaption.open(file)
        require(context.videoCaption.isOpened) { throwLiteFlowException(ErrorCodeEnum.VIDEO_READ_ERROR) }
        context.videoCaption.set(Videoio.CAP_PROP_POS_FRAMES, 0.0)
    }
}

@LiteflowComponent("finishProject")
class FinishProjectComponent(private val projectService: ProjectService) : NodeComponent() {
    override fun process() {
        context<ProjectContext>().let {
            it.videoCaption.release()
            projectService.closeProject(it.projectId)
        }
    }

    override fun isAccess(): Boolean {
        return context<ProjectContext>().videoCaption.isOpened
    }
}

@LiteflowComponent("filter")
class FilterComponent : NodeWhileComponent() {
    override fun processWhile(): Boolean {
        val cvContext = context<CvContext>()
        cvContext.release()
        val projectContext = context<ProjectContext>()
        val result = projectContext.videoCaption.grab()
        if (result) {
            val mat = Mat()
            projectContext.videoCaption.retrieve(Mat())
            cvContext.matStack.add(mat)
        }
        return result
    }
}

@LiteflowComponent("checkMatChannel")
class CheckMatChannelComponent : NodeIfComponent() {
    override fun processIf(): Boolean {
        require(context<CvContext>().matStack.last().channels() == 1) {
            throwLiteFlowException(ErrorCodeEnum.INVALID_MAT_CHANNEL)
        }
        return true
    }

}

@LiteflowComponent("saveMat")
class SaveMatComponent(private val fileService: FileService) : NodeComponent() {
    override fun process() {
        val projectContext = context<ProjectContext>()
        val cvContext = context<CvContext>()
        fileService.saveMat(projectContext.projectId, cvContext.matStack.size, cvContext.matStack.last())
    }
}