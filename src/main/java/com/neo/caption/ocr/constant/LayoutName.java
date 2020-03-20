package com.neo.caption.ocr.constant;

/**
 * The file's name in '/layout'
 */
public enum LayoutName {

    LAYOUT_BAT("Bat"),
    LAYOUT_BAT_NODE("BatNode"),
    LAYOUT_FILTER("Filter"),
    LAYOUT_MAIN("Main"),
    LAYOUT_MAT_NODE("MatNode"),
    LAYOUT_SETTINGS("Settings"),
    LAYOUT_MODULE_NODE("ModuleNode"),
    LAYOUT_SPLASH("Splash")
    ;

    private final String name;

    LayoutName(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }
}
