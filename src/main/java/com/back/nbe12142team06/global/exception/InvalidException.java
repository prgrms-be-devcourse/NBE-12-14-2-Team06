package com.back.nbe12142team06.global.exception;

public class InvalidException extends BusinessException{

    public InvalidException(String message) {
        super("400", message);
    }

    public InvalidException(int statusCode, String message) {
        super("400-" + String.valueOf(statusCode), message);
    }

    public InvalidException(String message, Throwable cause) {
        super("400", message, cause);
    }

    public InvalidException(int statusCode, String message, Throwable cause) {
        super("400-" + String.valueOf(statusCode), message, cause);
    }
}
