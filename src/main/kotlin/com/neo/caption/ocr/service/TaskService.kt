package com.neo.caption.ocr.service

import com.neo.caption.ocr.domain.dto.TaskConfigDto
import com.neo.caption.ocr.domain.entity.TaskConfig
import com.neo.caption.ocr.domain.vo.CaptionRowVo
import com.neo.caption.ocr.domain.vo.TaskScheduleVo

interface TaskService {

    fun initTask(projectId: String, taskConfigDto: TaskConfigDto): TaskConfig

    fun getTaskConfig(projectId: String): TaskConfig

    fun closeTask(taskId: String)

    fun getCaptionRowVoList(taskId: String): List<CaptionRowVo>

    fun updateCaptionRowVoList(taskId: String, captionRowVoList: List<CaptionRowVo>): List<CaptionRowVo>

    fun getSchedule(taskId: String): TaskScheduleVo

    fun updateSchedule(taskId: String, taskScheduleVo: TaskScheduleVo): TaskScheduleVo

}