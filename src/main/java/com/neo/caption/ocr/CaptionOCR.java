package com.neo.caption.ocr;

import com.neo.caption.ocr.stage.MainApplication;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@SpringBootApplication
@EnableAspectJAutoProxy
public class CaptionOCR {

    public static void main(String... args) {
        Application.launch(MainApplication.class, args);
    }
}
