package com.neo.caption.ocr.constant;

public enum FileType {

    // image
    BMP("424D", "BMP", "*.bmp", "*.dib"),
    JPEG("FFD8FF", "JPEG", "*.jpg", "*.jpeg"),
    PNG("89504E47", "PNG", "*.png"),

    // gz
    COCR("1F8B0800", "COCR", "*.cocr"),
    GZIP("1F8B0808", "GZIP", "*.gz"),

    // txt files do not have header.
    ASS("", "Advanced SubStation Alpha", "*.ass"),
    SRT("", "SubRipper", "*.srt"),
    JSON("", "Json File", "*.json"),

    // video
    VIDEO("", "Video File", "*.avi", "*.asf", "*.flv", "*.mkv", "*.mka", "*.mks", "*.mk3d",
            "*.mp4", "*.mpg", "*.mpeg", "*.mov", "*.rm", "*.rmvb", "*.vob", "*.webm", "*.wma", "*.wmv"),

    ALL("", "All Types", "*.*");

    private final String header;
    private final String description;
    private final String[] extensions;

    FileType(String header, String description, String... extensions) {
        this.header = header;
        this.description = description;
        this.extensions = extensions;
    }

    public String getHeader() {
        return header;
    }

    public String getDescription() {
        return description;
    }

    public String[] getExtensions() {
        return extensions;
    }
}
