package com.neo.caption.ocr.util;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.neo.caption.ocr.constant.ModuleProfile;
import com.neo.caption.ocr.constant.PrefKey;
import com.neo.caption.ocr.pojo.ModuleStatus;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import static com.neo.caption.ocr.constant.PrefKey.*;
import static com.neo.caption.ocr.util.BaseUtil.*;

@Component
@Getter
@Slf4j
public class PrefUtil {

    private final Preferences preferences;
    private final Gson gson;
    private final Joiner joiner;

    private ResourceBundle configBundle;

    /**
     * module profile index: the index of the profile list.
     */
    private int moduleProfileIndex;

    /**
     * min pixel count: Minimum threshold for the number of pixels in a image.
     * If the pixel is below than this value, the image is invalid.
     */
    private int minPixelCount;

    /**
     * structural similarity index, the bigger the better
     */
    private double minSSIMThreshold;

    /**
     * peak signal to noise ratio, the bigger the better
     */
    private double minPSNRThreshold;

    /**
     * similarity type: how to comparison image
     */
    private int similarityType;

    /**
     * storage policy: How to store when two or more images are similar
     */
    private int storagePolicy;

    /**
     * load how much items one time
     */
    private int countPerPage;

    /**
     * Enable dark theme
     */
    private boolean darkTheme;

    /**
     * Main stage background image
     * null: empty
     */
    private String backgroundImage;

    /**
     * background opacity
     */
    private int backgroundOpacity;

    private boolean compressImage;

    private int frameInterval;
    private int editorFontSize;
    private String tessLang;
    private String fileChooseDir;
    private String defaultStyle;
    private String digitalContainerFormat;
    private List<ModuleStatus> moduleStatusList;

    public PrefUtil(Preferences preferences, Gson gson, @Qualifier("dot") Joiner joiner) {
        this.preferences = preferences;
        this.gson = gson;
        this.joiner = joiner;
    }

    @PostConstruct
    public void init() {
        this.configBundle = ResourceBundle.getBundle("config");
        loadDataV2();
    }

    private void loadDataV2() {
        this.moduleProfileIndex = getInt(MODULE_PROFILE_INDEX);
        this.minPixelCount = getInt(MIN_PIXEL_COUNT);
        this.minSSIMThreshold = getDouble(MIN_SSIM_THRESHOLD);
        this.minPSNRThreshold = getDouble(MIN_PSNR_THRESHOLD);
        this.similarityType = getInt(SIMILARITY_TYPE);
        this.storagePolicy = getInt(STORAGE_POLICY);
        this.countPerPage = getInt(COUNT_PRE_PAGE);
        this.frameInterval = getInt(FRAME_INTERVAL);
        this.editorFontSize = getInt(EDITOR_FONT_SIZE);
        this.fileChooseDir = get(FILE_CHOOSE_DIR);
        this.defaultStyle = get(DEFAULT_STYLE);
        this.digitalContainerFormat = get(DIGITAL_CONTAINER_FORMAT);
        this.tessLang = get(TESS_LANG);
        this.darkTheme = getBoolean(DARK_THEME);
        this.backgroundImage = get(BACKGROUND_IMAGE);
        this.backgroundOpacity = getInt(BACKGROUND_OPACITY);
        this.compressImage = getBoolean(COMPRESS_IMAGE);
        loadModuleList();
    }

    public void loadModuleList() {
        String moduleJson;
        switch (ModuleProfile.values()[moduleProfileIndex]) {
            case HLS_COLOR:
                moduleJson = get(MODULE_PROFILE_HLS_COLOR);
                break;
            case HSV_COLOR:
                moduleJson = get(MODULE_PROFILE_HSV_COLOR);
                break;
            case FIXED_BINARY:
                moduleJson = get(MODULE_PROFILE_FIXED_BINARY);
                break;
            case ADAPTIVE_BINARY:
                moduleJson = get(MODULE_PROFILE_ADAPTIVE_BINARY);
                break;
            case CUSTOMIZE:
            default:
                moduleJson = get(MODULE_PROFILE_CUSTOMIZE);
                break;
        }
        this.moduleStatusList = new ArrayList<>(Arrays.asList(gson.fromJson(moduleJson, ModuleStatus[].class)));
    }

    private String getProperty(PrefKey key) {
        return configBundle.getString(joiner.join("cocr", key.toLowerCase()));
    }

    private int getInt(PrefKey key) {
        return preferences.getInt(key.toLowerCase(), s2i(getProperty(key)));
    }

    private double getDouble(PrefKey key) {
        return preferences.getDouble(key.toLowerCase(), s2d(getProperty(key)));
    }

    private boolean getBoolean(PrefKey key) {
        return preferences.getBoolean(key.toLowerCase(), s2b(getProperty(key)));
    }

    private String get(PrefKey key) {
        return preferences.get(key.toLowerCase(), getProperty(key));
    }

    public <V> void put(PrefKey key, V value) {
        switch (value.getClass().getSimpleName()) {
            case "Integer":
                preferences.putInt(key.toLowerCase(), (Integer) value);
                break;
            case "Double":
                preferences.putDouble(key.toLowerCase(), (Double) value);
                break;
            case "String":
                preferences.put(key.toLowerCase(), (String) value);
                break;
            case "Long":
                preferences.putLong(key.toLowerCase(), (Long) value);
                break;
            case "Boolean":
                preferences.putBoolean(key.toLowerCase(), (Boolean) value);
                break;
            case "Float":
                preferences.putFloat(key.toLowerCase(), (Float) value);
                break;
        }
    }

    public final void removePref(PrefKey... keys) {
        for (PrefKey key : keys) {
            preferences.remove(key.toLowerCase());
        }
        loadDataV2();
    }

    public final void setStoragePolicy(int storagePolicy) {
        this.storagePolicy = storagePolicy;
        put(STORAGE_POLICY, storagePolicy);
    }

    public final void setMinSSIMThreshold(double minSSIMThreshold) {
        this.minSSIMThreshold = minSSIMThreshold;
        put(MIN_SSIM_THRESHOLD, minSSIMThreshold);
    }

    public final void setMinPSNRThreshold(double minPSNRThreshold) {
        this.minPSNRThreshold = minPSNRThreshold;
        put(MIN_PSNR_THRESHOLD, minPSNRThreshold);
    }

    public final void setMinPixelCount(int minPixelCount) {
        this.minPixelCount = minPixelCount;
        put(MIN_PIXEL_COUNT, minPixelCount);
    }

    public final void setSimilarityType(int similarityType) {
        this.similarityType = similarityType;
        put(SIMILARITY_TYPE, similarityType);
    }

    public final void setCountPerPage(int countPerPage) {
        this.countPerPage = countPerPage;
        put(COUNT_PRE_PAGE, countPerPage);
    }

    public final void setFrameInterval(int frameInterval) {
        this.frameInterval = frameInterval;
        put(FRAME_INTERVAL, frameInterval);
    }

    public final void setEditorFontSize(int editorFontSize) {
        this.editorFontSize = editorFontSize;
        put(EDITOR_FONT_SIZE, editorFontSize);
    }

    public final void setTessLang(String tessLang) {
        this.tessLang = tessLang;
        put(TESS_LANG, tessLang);
    }

    public final void setFileChooseDir(String fileChooseDir) {
        this.fileChooseDir = fileChooseDir;
        put(FILE_CHOOSE_DIR, fileChooseDir);
    }

    public final void setDefaultStyle(String defaultStyle) {
        this.defaultStyle = defaultStyle;
        put(DEFAULT_STYLE, defaultStyle);
    }

    public final void setDigitalContainerFormat(String digitalContainerFormat) {
        this.digitalContainerFormat = digitalContainerFormat;
        put(DIGITAL_CONTAINER_FORMAT, digitalContainerFormat);
    }

    public final void setModuleStatusList(List<ModuleStatus> moduleStatusList) {
        this.moduleStatusList = moduleStatusList;
        switch (ModuleProfile.values()[moduleProfileIndex]) {
            case HLS_COLOR:// hls
                put(MODULE_PROFILE_HLS_COLOR, gson.toJson(moduleStatusList));
                break;
            case HSV_COLOR:// hsv
                put(MODULE_PROFILE_HSV_COLOR, gson.toJson(moduleStatusList));
                break;
            case ADAPTIVE_BINARY:// morphology
                put(MODULE_PROFILE_ADAPTIVE_BINARY, gson.toJson(moduleStatusList));
                break;
            case FIXED_BINARY:
                put(MODULE_PROFILE_FIXED_BINARY, gson.toJson(moduleStatusList));
                break;
            case CUSTOMIZE:
            default:
                put(MODULE_PROFILE_CUSTOMIZE, gson.toJson(moduleStatusList));
                break;
        }
    }

    public final void setModuleProfileIndex(int moduleProfileIndex) {
        this.moduleProfileIndex = moduleProfileIndex;
        put(MODULE_PROFILE_INDEX, moduleProfileIndex);
    }

    public final void setDarkTheme(boolean darkTheme) {
        this.darkTheme = darkTheme;
        put(DARK_THEME, darkTheme);
    }

    public final void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
        put(BACKGROUND_IMAGE, backgroundImage);
    }

    public final void setBackgroundOpacity(int backgroundOpacity) {
        this.backgroundOpacity = backgroundOpacity;
        put(BACKGROUND_OPACITY, backgroundOpacity);
    }

    public final void setCompressImage(boolean compressImage) {
        this.compressImage = compressImage;
        put(COMPRESS_IMAGE, compressImage);
    }
}
