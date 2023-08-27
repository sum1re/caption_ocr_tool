package com.neo.caption.ocr.service.impl

import com.neo.caption.ocr.annotation.Slf4j
import com.neo.caption.ocr.domain.entity.TaskConfig
import com.neo.caption.ocr.domain.vo.ProjectVo
import com.neo.caption.ocr.service.FileService
import com.neo.caption.ocr.service.ProjectService
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Slf4j
@Service
@CacheConfig(cacheNames = ["project::"])
class ProjectServiceImpl(
    private val fileService: FileService,
) : ProjectService {

    override fun initProject() = ProjectVo(fileService.createWorkingDirectory())

    @Cacheable(key = "#p0")
    override fun getProjectTaskConfig(projectId: String) = TaskConfig()

    @CachePut(key = "#p0")
    override fun updateTaskConfig(projectId: String, taskConfig: TaskConfig) = taskConfig

    @CacheEvict(key = "#p0")
    override fun closeProject(projectId: String) {}

}