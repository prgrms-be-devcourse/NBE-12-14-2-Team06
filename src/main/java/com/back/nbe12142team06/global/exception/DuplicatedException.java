package com.back.nbe12142team06.global.exception;

public class DuplicatedException extends BusinessException {
    public DuplicatedException(String message) {
        super("409", message);
    }

    public DuplicatedException(int statusCode, String message) {
        super("409-" + String.valueOf(statusCode), message);
    }

    public DuplicatedException(String message, Throwable cause) {
        super("409", message, cause);
    }

    public DuplicatedException(int statusCode, String message, Throwable cause) {
        super("409-" + String.valueOf(statusCode), message, cause);
    }
}
