package com.back.nbe12142team06.global.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    String statusCode;

    public BusinessException(String statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public BusinessException(String statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
