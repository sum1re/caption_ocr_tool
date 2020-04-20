package com.neo.caption.ocr.constant;

import java.io.File;

public class Dir {

    public static final String USR_DIR = System.getProperty("cocr.dir");
    public static final File SPLASH_DIR = new File(USR_DIR, "../splash");
    public static final File TESS_DATA_DIR = new File(USR_DIR, "tessdata");
    public static final File MODULE_PROFILE_DIR = new File(USR_DIR, "moduleProfiles");

}
