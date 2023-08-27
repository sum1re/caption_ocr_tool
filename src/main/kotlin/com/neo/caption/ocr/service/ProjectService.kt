package com.neo.caption.ocr.service

import com.neo.caption.ocr.domain.entity.TaskConfig
import com.neo.caption.ocr.domain.vo.ProjectVo

interface ProjectService {

    fun initProject(): ProjectVo

    fun getProjectTaskConfig(projectId: String): TaskConfig

    fun updateTaskConfig(projectId: String, taskConfig: TaskConfig): TaskConfig

    fun closeProject(projectId: String)

}