package com.neo.caption.ocr.stage;

import com.neo.caption.ocr.CaptionOCR;
import com.neo.caption.ocr.util.DataUtil;
import com.neo.caption.ocr.util.ModuleProfileWatcher;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Core;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

import static javafx.application.Preloader.StateChangeNotification.Type.BEFORE_START;

@Slf4j
public class MainApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        context = new SpringApplicationBuilder(CaptionOCR.class).run();
    }

    @Override
    public void start(Stage stage) throws IOException {
        context.publishEvent(new StageEvent(stage));
        DataUtil dataUtil = context.getBean(DataUtil.class);
        dataUtil.dealOldData();
        notifyPreloader(new Preloader.StateChangeNotification(BEFORE_START));
    }

    @Override
    public void stop() {
        // close file watcher service
        context.getBean(ModuleProfileWatcher.class)
                .close();
        context.close();
        Platform.exit();
    }

    public static class StageEvent extends ApplicationEvent {

        private static final long serialVersionUID = -2677123198031625706L;

        StageEvent(Object source) {
            super(source);
        }

        Stage getStage() {
            return (Stage) getSource();
        }
    }
}
