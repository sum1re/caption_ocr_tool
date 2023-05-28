package com.neo.caption.ocr.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ViewController {

    @GetMapping(path = ["/", "/index"])
    fun index() = "index"

}