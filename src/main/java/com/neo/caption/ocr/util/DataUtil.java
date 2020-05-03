package com.neo.caption.ocr.util;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.neo.caption.ocr.constant.PrefKey;
import com.neo.caption.ocr.pojo.ModuleStatus;
import com.neo.caption.ocr.service.FileService;
import com.neo.caption.ocr.service.PreferencesService;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.neo.caption.ocr.constant.PrefKey.*;

@Component
public class DataUtil {

    private final FileService fileService;
    private final Gson gson;
    private final PreferencesService preferencesService;

    public DataUtil(FileService fileService, Gson gson, PreferencesService preferencesService) {
        this.fileService = fileService;
        this.gson = gson;
        this.preferencesService = preferencesService;
    }

    @SuppressWarnings("deprecation")
    public void dealOldData() throws IOException {
        if (preferencesService.exists(MODULE_PROFILE_INDEX)) {
            preferencesService.remove(MODULE_PROFILE_INDEX);
        }
        if (preferencesService.exists(MODULE_STATUS_LIST)) {
            preferencesService.remove(MODULE_STATUS_LIST);
        }
        saveProfileToFile(MODULE_PROFILE_FIXED_BINARY, "FixedBinary");
        saveProfileToFile(MODULE_PROFILE_ADAPTIVE_BINARY, "AdaptiveBinary");
        saveProfileToFile(MODULE_PROFILE_HLS_COLOR, "HLS");
        saveProfileToFile(MODULE_PROFILE_HSV_COLOR, "HSV");
        saveProfileToFile(MODULE_PROFILE_CUSTOMIZE, "Customer");
    }

    private void saveProfileToFile(PrefKey prefKey, String name) throws IOException {
        if (preferencesService.exists(prefKey)) {
            String json = preferencesService.getString(prefKey, null);
            if (!Strings.isNullOrEmpty(json)) {
                fileService.saveModuleProfile(name, gson.fromJson(json, ModuleStatus[].class));
            }
            preferencesService.remove(prefKey);
        }
    }

}
