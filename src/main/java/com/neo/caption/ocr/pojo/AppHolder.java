package com.neo.caption.ocr.pojo;

import com.neo.caption.ocr.constant.FileType;
import com.neo.caption.ocr.view.MatNode;
import javafx.application.HostServices;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * To hold some global objects
 */
@Data
@Component
@Slf4j
public class AppHolder {

    private Set<Stage> stageList;
    private List<MatNode> matNodeList;
    private List<String> moduleProfileList;
    private String ocr;
    private int matNodeSelectedIndex;
    private ThreadLocal<StringBuilder> stringBuilderThreadLocal;
    //FileChooser
    private Map<FileType, FileChooser.ExtensionFilter> filterMap;
    private HostServices hostServices;

    @PostConstruct
    public void init() {
        this.matNodeList = new ArrayList<>(64);
        this.moduleProfileList = new ArrayList<>(8);
        this.ocr = "";
        this.matNodeSelectedIndex = 0;
        this.stringBuilderThreadLocal = ThreadLocal.withInitial(() -> new StringBuilder(1024));
        this.stageList = new HashSet<>(8);
        loadFilter();
    }

    public int getCount() {
        return matNodeList.size();
    }

    public StringBuilder getStringBuilder() {
        StringBuilder stringBuilder = stringBuilderThreadLocal.get();
        stringBuilder.setLength(0);
        return stringBuilder;
    }

    private void loadFilter() {
        this.filterMap = new HashMap<>(16);
        for (FileType fileType : FileType.values()) {
            FileChooser.ExtensionFilter filter =
                    new FileChooser.ExtensionFilter(fileType.getDescription(), fileType.getExtensions());
            filterMap.put(fileType, filter);
        }
    }

    public FileChooser.ExtensionFilter[] getExtFilter(FileType... fileTypes) {
        Set<FileChooser.ExtensionFilter> filterSet = new LinkedHashSet<>(fileTypes.length);
        for (FileType fileType : fileTypes) {
            filterSet.add(filterMap.get(fileType));
        }
        return filterSet.toArray(new FileChooser.ExtensionFilter[0]);
    }

}
