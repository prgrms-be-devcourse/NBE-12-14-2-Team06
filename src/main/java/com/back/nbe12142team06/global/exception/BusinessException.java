package com.back.nbe12142team06.global.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    String statusCode;

    public BusinessException(String statusCode, String message) {
        this.statusCode = statusCode;
        super(message);
    }

    public BusinessException(String statusCode, String message, Throwable cause) {
        this.statusCode = statusCode;
        super(message, cause);
    }
}
