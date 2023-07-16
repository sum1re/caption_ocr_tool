package com.neo.caption.ocr.domain.mapper

import com.neo.caption.ocr.domain.entity.TaskConfig
import com.neo.caption.ocr.domain.vo.TaskVo
import org.springframework.stereotype.Component

@Component
class TaskMapper {

    fun toVo(taskConfig: TaskConfig): TaskVo {
        return TaskVo(taskConfig.taskId, taskConfig.videoInfo!!.totalFrame)
    }

}