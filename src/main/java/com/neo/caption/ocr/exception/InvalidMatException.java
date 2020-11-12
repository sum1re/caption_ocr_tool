package com.neo.caption.ocr.exception;

public final class InvalidMatException extends Exception {

    private static final long serialVersionUID = 2785597452213724327L;

    public InvalidMatException() {
        super();
    }

    public InvalidMatException(String message) {
        super(message);
    }

    public InvalidMatException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidMatException(Throwable cause) {
        super(cause);
    }

    protected InvalidMatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
