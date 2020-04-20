package com.neo.caption.ocr.util;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.neo.caption.ocr.pojo.AppHolder;
import com.neo.caption.ocr.service.FileService;
import com.neo.caption.ocr.service.PreferencesService;
import com.neo.caption.ocr.stage.StageBroadcast;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.neo.caption.ocr.constant.Dir.MODULE_PROFILE_DIR;
import static com.neo.caption.ocr.constant.PrefKey.MODULE_PROFILE_DEFAULT;
import static com.neo.caption.ocr.constant.PrefKey.MODULE_PROFILE_NAME;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

@Component
@Slf4j
public class ModuleProfileWatcher {

    private final AppHolder appHolder;
    private final ExecutorService executorService;
    private final FileService fileService;
    private final PreferencesService preferencesService;
    private final StageBroadcast stageBroadcast;

    public ModuleProfileWatcher(AppHolder appHolder, ExecutorService executorService, FileService fileService,
                                PreferencesService preferencesService, StageBroadcast stageBroadcast) {
        this.appHolder = appHolder;
        this.executorService = executorService;
        this.fileService = fileService;
        this.preferencesService = preferencesService;
        this.stageBroadcast = stageBroadcast;
    }

    @PostConstruct
    public void init() {
        executorService.execute(() -> {
            try {
                task();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void task() throws IOException, InterruptedException {
        verifyListAndFile();
        WatchService watchService = FileSystems.getDefault().newWatchService();
        MODULE_PROFILE_DIR.toPath().register(watchService, ENTRY_CREATE, ENTRY_DELETE);
        WatchKey watchKey;
        do {
            watchKey = watchService.take();
            watchKey.pollEvents()
                    .stream()
                    .filter(watchEvent -> getWatchEventContext(watchEvent).endsWith(".json"))
                    .forEach(watchEvent -> {
                        WatchEvent.Kind<?> kind = watchEvent.kind();
                        if (ENTRY_DELETE.equals(kind)) {
                            listRemove(watchEvent);
                            addDefaultProfile();
                        } else if (ENTRY_CREATE.equals(kind)) {
                            listAdd(watchEvent);
                        }
                        appHolder.getModuleProfileList().sort(Comparator.naturalOrder());
                        Platform.runLater(stageBroadcast::sendProfileListBroadcast);
                    });
        } while (watchKey.reset());
    }

    private void verifyListAndFile() throws IOException {
        List<String> tempList = appHolder.getModuleProfileList();
        String tempName = MODULE_PROFILE_NAME.stringValue();
        if (Strings.isNullOrEmpty(tempName) || tempList.isEmpty() || !tempList.contains(tempName)) {
            if (!tempList.contains("Default")) {
                tempList.add("Default");
                fileService.saveModuleProfile("Default", MODULE_PROFILE_DEFAULT.value());
            }
            preferencesService.put(MODULE_PROFILE_NAME, "Default");
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void listAdd(WatchEvent<?> event) {
        appHolder.getModuleProfileList()
                .add(Files.getNameWithoutExtension(getWatchEventContext(event)));
    }

    @SuppressWarnings("UnstableApiUsage")
    private void listRemove(WatchEvent<?> event) {
        appHolder.getModuleProfileList()
                .remove(Files.getNameWithoutExtension(getWatchEventContext(event)));
    }

    private String getWatchEventContext(WatchEvent<?> event) {
        return ((Path) event.context()).getFileName().toString();
    }

    private void addDefaultProfile() {
        if (!appHolder.getModuleProfileList().isEmpty()) {
            preferencesService.put(MODULE_PROFILE_NAME, appHolder.getModuleProfileList().get(0));
            return;
        }
        try {
            fileService.saveModuleProfile("Default", MODULE_PROFILE_DEFAULT.value());
            preferencesService.put(MODULE_PROFILE_NAME, "Default");
        } catch (IOException ignored) {
        }
    }

}
