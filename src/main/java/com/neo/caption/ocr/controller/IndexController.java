package com.neo.caption.ocr.controller;

import com.neo.caption.ocr.service.AppInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@Slf4j
@RequiredArgsConstructor
public class IndexController {

    private final AppInfoService appInfoService;

    @GetMapping
    public ResponseEntity<Object> getInfo() {
        var result = appInfoService.getInfo();
        return ResponseEntity.ok(result);
    }

}
