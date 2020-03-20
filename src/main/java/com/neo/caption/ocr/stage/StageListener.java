package com.neo.caption.ocr.stage;

import com.neo.caption.ocr.util.FxUtil;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static com.neo.caption.ocr.constant.LayoutName.LAYOUT_MAIN;

@Component
@Slf4j
public class StageListener implements ApplicationListener<MainApplication.StageEvent> {

    private final FxUtil fxUtil;

    public StageListener(FxUtil fxUtil) {
        this.fxUtil = fxUtil;
    }

    @Override
    public void onApplicationEvent(@Nonnull MainApplication.StageEvent stageEvent) {
        log.debug("Start applicationEvent");
        try {
            Stage stage = stageEvent.getStage();
            fxUtil.loadStage(stage, LAYOUT_MAIN, "stage.title.main");
            stage.show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
