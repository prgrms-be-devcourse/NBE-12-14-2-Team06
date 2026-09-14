package com.back.nbe12142team06.global.exception;

public class NotFoundException extends BusinessException {
    public NotFoundException(String message) {
        super("404", message);
    }

    public NotFoundException(int statusCode, String message) {
        super("404-" + String.valueOf(statusCode), message);
    }

    public NotFoundException(String message, Throwable cause) {
        super("404", message, cause);
    }

    public NotFoundException(int statusCode, String message, Throwable cause) {
        super("404-" + String.valueOf(statusCode), message, cause);
    }
}
