package com.neo.caption.ocr.stage;

import javafx.beans.property.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.neo.caption.ocr.util.BaseUtil.getTimeGen;

@Component
public class StageBroadcast {

    private LongProperty digitalBroadcast;
    private LongProperty tessLangBroadcast;
    private LongProperty moduleBroadcast;
    private LongProperty dataEmptyBroadcast;
    private IntegerProperty editorBroadcast;
    private LongProperty backgroundImageBroadcast;

    @PostConstruct
    public void init() {
        this.moduleBroadcast = new SimpleLongProperty();
        this.digitalBroadcast = new SimpleLongProperty();
        this.tessLangBroadcast = new SimpleLongProperty();
        this.dataEmptyBroadcast = new SimpleLongProperty();
        this.editorBroadcast = new SimpleIntegerProperty();
        this.backgroundImageBroadcast = new SimpleLongProperty();
    }

    public void sendModuleBroadcast() {
        this.moduleBroadcast.set(getTimeGen());
    }

    public void sendDigitalBroadcast() {
        this.digitalBroadcast.set(getTimeGen());
    }

    public void sendTessLangBroadcast() {
        this.tessLangBroadcast.set(getTimeGen());
    }

    public void sendDataEmptyBroadcast() {
        this.dataEmptyBroadcast.set(getTimeGen());
    }

    public void sendEditorBroadcast(int value) {
        this.editorBroadcast.set(value);
    }

    public void sendBackgroundImageBroadcast() {
        this.backgroundImageBroadcast.set(getTimeGen());
    }

    public LongProperty digitalBroadcast() {
        return digitalBroadcast;
    }

    public LongProperty moduleBroadcast() {
        return moduleBroadcast;
    }

    public LongProperty tessLangBroadcast() {
        return tessLangBroadcast;
    }

    public LongProperty dataEmptyBroadcast() {
        return dataEmptyBroadcast;
    }

    public IntegerProperty editorBroadcast() {
        return editorBroadcast;
    }

    public LongProperty backgroundImageBroadcast() {
        return backgroundImageBroadcast;
    }
}
