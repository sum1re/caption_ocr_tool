package com.neo.caption.ocr.module.viewer

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ViewerController {

    @GetMapping(path = ["/", "/index"])
    fun index() = "index"

}