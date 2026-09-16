package com.back.nbe12142team06.global.exception;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super("401", message);
    }

    public UnauthorizedException(int statusCode, String message) {
        super("401-" + String.valueOf(statusCode), message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super("401", message, cause);
    }

    public UnauthorizedException(int statusCode, String message, Throwable cause) {
        super("401-" + String.valueOf(statusCode), message, cause);
    }
}
