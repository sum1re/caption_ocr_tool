package com.neo.caption.ocr.module.project

import com.neo.caption.ocr.common.RestEntityController
import com.neo.caption.ocr.module.app.AppService
import com.neo.caption.ocr.support.response
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@RestEntityController("/api/project")
class ProjectController(
    private val projectService: ProjectService,
    private val appService: AppService,
) {

    @GetMapping("/run/{projectId}")
    fun startProject(@PathVariable projectId: UUID) = response {

    }

    @GetMapping("/{projectId}")
    fun getProjectMetadata(@PathVariable projectId: UUID) = response {

    }

    @GetMapping("/all")
    fun getProjects() = response {
    }

    @GetMapping("/caption/{projectId}")
    fun getCaptionRowList(
        @PathVariable projectId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "100") size: Int,
    ) = response {
    }

    @PostMapping
    fun createProject() = response {
    }

    @PutMapping("/{projectId}")
    fun updateProject(
        @PathVariable projectId: UUID,
        @RequestParam astModelId: UUID?,
        @RequestParam ocrProfileId: UUID?,
    ) = response {
    }

    @DeleteMapping("/caption/{projectId}/{deletedIds}")
    fun deleteCaptionRow(
        @PathVariable projectId: UUID,
        @PathVariable deletedIds: List<Long>,
    ) = response {
    }

    @DeleteMapping("/{projectId}")
    fun deleteProject(@PathVariable projectId: UUID) = response {
    }

}