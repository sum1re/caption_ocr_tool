package com.neo.caption.ocr.exception;

public final class InvalidMatNodesException extends Exception {

    private static final long serialVersionUID = -4493921731103981240L;

    public InvalidMatNodesException() {
        super();
    }

    public InvalidMatNodesException(String message) {
        super(message);
    }

    public InvalidMatNodesException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidMatNodesException(Throwable cause) {
        super(cause);
    }

    protected InvalidMatNodesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
