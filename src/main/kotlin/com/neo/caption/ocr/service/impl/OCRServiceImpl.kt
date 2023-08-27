package com.neo.caption.ocr.service.impl

import com.neo.caption.ocr.annotation.Slf4j
import com.neo.caption.ocr.constant.ErrorCodeEnum
import com.neo.caption.ocr.domain.entity.TesseractConfig
import com.neo.caption.ocr.exception.BadRequestException
import com.neo.caption.ocr.service.OCRService
import com.neo.caption.ocr.service.TesseractService
import com.neo.caption.ocr.toByteArray
import org.bytedeco.tesseract.TessBaseAPI
import org.opencv.core.Mat
import org.springframework.stereotype.Service

@Slf4j
@Service
class OCRServiceImpl(
    private val tesseractService: TesseractService,
) : OCRService {

    private var apiMap: MutableMap<String, TessBaseAPI> = mutableMapOf()

    override fun initial(tesseractConfig: TesseractConfig, projectId: String) {
        apiMap[projectId] = tesseractService.initTessBaseApi(tesseractConfig)
    }

    override fun release(projectId: String) {
        getTessBaseAPI(projectId).releaseReference()
        apiMap.remove(projectId)
    }

    override fun doOCR(projectId: String, mat: Mat): String {
        val api = getTessBaseAPI(projectId)
        return doOCR(api, mat)
    }

    fun doOCR(projectId: String, matList: List<Mat>): List<String> {
        val api = getTessBaseAPI(projectId)
        return matList.map { doOCR(api, it) }.toList()
    }

    private fun getTessBaseAPI(projectId: String) =
        apiMap[projectId] ?: throw BadRequestException(ErrorCodeEnum.INVALID_PARAMETER, "failed to invoke tesseract")

    private fun doOCR(tessBaseAPI: TessBaseAPI, mat: Mat): String {
        tessBaseAPI.SetImage(mat.toByteArray(), mat.cols(), mat.rows(), mat.channels(), mat.cols())
        return tessBaseAPI.GetUTF8Text().use { if (it.isNull) "" else it.string }
    }

}