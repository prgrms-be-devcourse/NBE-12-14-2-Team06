package com.back.nbe12142team06.global.exception;

public class ForbiddenException extends BusinessException{

    public ForbiddenException(String message) {
        super("403", message);
    }

    public ForbiddenException(int statusCode, String message) {
        super("403-" + String.valueOf(statusCode), message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super("403", message, cause);
    }

    public ForbiddenException(int statusCode, String message, Throwable cause) {
        super("403-" + String.valueOf(statusCode), message, cause);
    }
}
