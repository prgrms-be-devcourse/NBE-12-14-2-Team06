package com.back.nbe12142team06.global.exception;

public class InternalServerErrorException extends BusinessException {
    public InternalServerErrorException(String message) {
        super("500", message);
    }

    public InternalServerErrorException(int statusCode, String message) {
        super("500-" + String.valueOf(statusCode), message);
    }

    public InternalServerErrorException(String message, Throwable cause) {
        super("500", message, cause);
    }

    public InternalServerErrorException(int statusCode, String message, Throwable cause) {
        super("500-" + String.valueOf(statusCode), message, cause);
    }
}
