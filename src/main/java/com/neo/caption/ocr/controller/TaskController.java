package com.neo.caption.ocr.controller;

import com.neo.caption.ocr.domain.dto.TaskAttributeDto;
import com.neo.caption.ocr.domain.mapper.TaskAttributeMapper;
import com.neo.caption.ocr.service.TaskService;
import com.neo.caption.ocr.service.TesseractService;
import com.neo.caption.ocr.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task")
@Slf4j
@RequiredArgsConstructor
public class TaskController {

    private final TesseractService tesseractService;
    private final VideoService videoService;
    private final TaskService taskService;
    private final TaskAttributeMapper taskAttributeMapper;

    @GetMapping("/language")
    public ResponseEntity<Object> getSupportedLanguage() {
        var result = tesseractService.getTessLanguageDtoList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/progress/{taskId}")
    public ResponseEntity<Object> getTaskProgress(@PathVariable String taskId) {
        var result = videoService.getFrameCount(taskId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/result/{taskId}")
    public ResponseEntity<Object> getTaskResult(@PathVariable String taskId) {
        var result = taskService.getOCRResult(taskId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Object> deleteTask(@PathVariable String taskId) {
        taskService.removeOCRResult(taskId);
        videoService.removeFrameCount(taskId);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<Object> startTask(@RequestBody @NotNull TaskAttributeDto taskAttributeDto) {
        var taskAttribute = taskService.initTask(taskAttributeDto);
        var result = taskAttributeMapper.toDto(taskAttribute);
        taskService.runTask(taskAttribute);
        return ResponseEntity.ok(result);
    }

}
