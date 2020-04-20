package com.neo.caption.ocr.service;

import com.neo.caption.ocr.constant.PrefKey;

public interface PreferencesService {

    <V> void put(PrefKey prefKey, V value);

    void remove(PrefKey... prefKeys);

    boolean exists(PrefKey prefKey);

    int getInt(PrefKey prefKey);

    double getDouble(PrefKey prefKey);

    boolean getBoolean(PrefKey prefKey);

    String getString(PrefKey prefKey, String def);

    String getString(PrefKey prefKey);
}
