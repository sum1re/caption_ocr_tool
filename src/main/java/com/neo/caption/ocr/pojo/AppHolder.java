package com.neo.caption.ocr.pojo;

import com.google.common.base.Splitter;
import com.neo.caption.ocr.view.MatNode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.neo.caption.ocr.constant.PrefKey.DIGITAL_CONTAINER_FORMAT;

/**
 * To hold some global objects
 */
@Data
@Component
@Slf4j
public class AppHolder {

    private Splitter splitter;

    private Set<Stage> stageList;
    private List<MatNode> matNodeList;
    private List<String> moduleProfileList;
    private String ocr;
    private int matNodeSelectedIndex;
    private ThreadLocal<StringBuilder> stringBuilderThreadLocal;
    //FileChooser
    private FileChooser.ExtensionFilter cocrFilter;
    private FileChooser.ExtensionFilter videoFilter;
    private FileChooser.ExtensionFilter imageFilter;
    private FileChooser.ExtensionFilter pngFilter;
    private FileChooser.ExtensionFilter captionFilter;
    private FileChooser.ExtensionFilter noneFilter;
    private FileChooser.ExtensionFilter jsonFilter;

    @Autowired
    public AppHolder(@Qualifier("comma") Splitter splitter) {
        this.splitter = splitter;
    }

    @PostConstruct
    public void init() {
        this.matNodeList = new ArrayList<>(64);
        this.moduleProfileList = new ArrayList<>(8);
        this.ocr = "";
        this.matNodeSelectedIndex = 0;
        this.stringBuilderThreadLocal = ThreadLocal.withInitial(() -> new StringBuilder(1024));
        this.cocrFilter = new FileChooser.ExtensionFilter("COCR File", "*.cocr");
        this.pngFilter = new FileChooser.ExtensionFilter("Image File", "*.png");
        this.imageFilter = new FileChooser.ExtensionFilter("Image File", "*.png", "*.jpg", "*.bmp");
        this.captionFilter = new FileChooser.ExtensionFilter("Caption File", "*.ass", "*.srt");
        this.noneFilter = new FileChooser.ExtensionFilter("All File", "*.*");
        this.jsonFilter = new FileChooser.ExtensionFilter("Json File", "*.json");
        this.stageList = new HashSet<>(8);
    }

    public int getCount() {
        return matNodeList.size();
    }

    public StringBuilder getStringBuilder() {
        StringBuilder stringBuilder = stringBuilderThreadLocal.get();
        stringBuilder.setLength(0);
        return stringBuilder;
    }

    public void loadVideoFilter() {
        this.videoFilter = new FileChooser.ExtensionFilter("Video File", splitter.splitToList(DIGITAL_CONTAINER_FORMAT.stringValue()));
    }

}
