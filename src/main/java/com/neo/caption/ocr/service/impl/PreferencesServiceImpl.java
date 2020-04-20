package com.neo.caption.ocr.service.impl;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.neo.caption.ocr.constant.PrefKey;
import com.neo.caption.ocr.pojo.AppHolder;
import com.neo.caption.ocr.pojo.ModuleStatus;
import com.neo.caption.ocr.service.PreferencesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import static com.neo.caption.ocr.constant.PrefKey.*;
import static com.neo.caption.ocr.util.BaseUtil.*;

@Service
@Slf4j
public class PreferencesServiceImpl implements PreferencesService {

    private final AppHolder appHolder;
    private final Gson gson;
    private final Joiner joiner;
    private final Preferences preferences;

    private ResourceBundle configBundle;

    public PreferencesServiceImpl(
            AppHolder appHolder, Gson gson, @Qualifier("dot") Joiner joiner, Preferences preferences) {
        this.appHolder = appHolder;
        this.gson = gson;
        this.joiner = joiner;
        this.preferences = preferences;
    }

    @PostConstruct
    public void init() {
        this.configBundle = ResourceBundle.getBundle("config");
        loadIntData(MIN_PIXEL_COUNT, SIMILARITY_TYPE, STORAGE_POLICY, COUNT_PRE_PAGE,
                FRAME_INTERVAL, EDITOR_FONT_SIZE, BACKGROUND_OPACITY);
        loadDoubleData(MIN_SSIM_THRESHOLD, MIN_PSNR_THRESHOLD);
        loadStringData(MODULE_PROFILE_NAME, FILE_CHOOSE_DIR, DEFAULT_STYLE,
                DIGITAL_CONTAINER_FORMAT, TESS_LANG, BACKGROUND_IMAGE);
        loadBooleanData(DARK_THEME, COMPRESS_IMAGE);
        MODULE_PROFILE_DEFAULT.setValue(gson.fromJson(getDefValue(MODULE_PROFILE_DEFAULT), ModuleStatus[].class));
        appHolder.loadVideoFilter();
    }

    /**
     * get default from config.properties
     *
     * @param prefKey /
     * @return /
     */
    private String getDefValue(PrefKey prefKey) {
        return configBundle.getString(joiner.join("cocr", prefKey.toLowerCase()));
    }

    @Override
    public <V> void put(PrefKey prefKey, V value) {
        prefKey.setValue(value);
        switch (value.getClass().getSimpleName()) {
            case "Integer":
                preferences.putInt(prefKey.toLowerCase(), (Integer) value);
                break;
            case "Double":
                preferences.putDouble(prefKey.toLowerCase(), (Double) value);
                break;
            case "String":
                preferences.put(prefKey.toLowerCase(), (String) value);
                break;
            case "Long":
                preferences.putLong(prefKey.toLowerCase(), (Long) value);
                break;
            case "Boolean":
                preferences.putBoolean(prefKey.toLowerCase(), (Boolean) value);
                break;
            case "Float":
                preferences.putFloat(prefKey.toLowerCase(), (Float) value);
                break;
        }
    }

    @Override
    public void remove(PrefKey... prefKeys) {
        for (PrefKey key : prefKeys) {
            preferences.remove(key.toLowerCase());
        }
    }

    @Override
    public boolean exists(PrefKey prefKey) {
        return preferences.get(prefKey.toLowerCase(), null) != null;
    }

    @Override
    public int getInt(PrefKey prefKey) {
        return preferences.getInt(prefKey.toLowerCase(), s2i(getDefValue(prefKey)));
    }

    @Override
    public double getDouble(PrefKey prefKey) {
        return preferences.getDouble(prefKey.toLowerCase(), s2d(getDefValue(prefKey)));
    }

    @Override
    public boolean getBoolean(PrefKey prefKey) {
        return preferences.getBoolean(prefKey.toLowerCase(), s2b(getDefValue(prefKey)));
    }

    @Override
    public String getString(PrefKey prefKey, String def) {
        return preferences.get(prefKey.toLowerCase(), def);
    }

    @Override
    public String getString(PrefKey prefKey) {
        return getString(prefKey, getDefValue(prefKey));
    }

    private void loadIntData(PrefKey... prefKeys) {
        for (PrefKey prefKey : prefKeys) {
            prefKey.setValue(getInt(prefKey));
        }
    }

    private void loadDoubleData(PrefKey... prefKeys) {
        for (PrefKey prefKey : prefKeys) {
            prefKey.setValue(getDouble(prefKey));
        }
    }

    private void loadStringData(PrefKey... prefKeys) {
        for (PrefKey prefKey : prefKeys) {
            prefKey.setValue(getString(prefKey));
        }
    }

    private void loadBooleanData(PrefKey... prefKeys) {
        for (PrefKey prefKey : prefKeys) {
            prefKey.setValue(getBoolean(prefKey));
        }
    }


}
