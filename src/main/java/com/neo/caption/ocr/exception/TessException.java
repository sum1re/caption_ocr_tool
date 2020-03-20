package com.neo.caption.ocr.exception;

public class TessException extends Exception {

    private static final long serialVersionUID = -3828708457937915809L;

    public TessException() {
        super();
    }

    public TessException(String message) {
        super(message);
    }

    public TessException(String message, Throwable cause) {
        super(message, cause);
    }

    public TessException(Throwable cause) {
        super(cause);
    }

    protected TessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
