package com.neo.caption.ocr.service

import com.neo.caption.ocr.common.CommonService
import com.neo.caption.ocr.module.file.FileService
import org.springframework.scheduling.annotation.Scheduled

@CommonService
class ScheduleService(
    private val fileService: FileService,
) {

    @Scheduled(fixedDelayString = "PT24H")
    fun cleanup() {
        fileService.cleanupExpired()
    }

}