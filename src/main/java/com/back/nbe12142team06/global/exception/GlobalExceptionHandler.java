package com.back.nbe12142team06.global.exception;

import com.back.nbe12142team06.global.response.RsData;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public RsData<?> businessExceptionHandler(BusinessException e) {
        return new RsData<>(e.statusCode, e.getMessage());
    }

    @ExceptionHandler
    public RsData<?> notFoundExceptionHandler(NotFoundException e) {
        return new RsData<>(e.statusCode, e.getMessage());
    }

    @ExceptionHandler
    public RsData<?> invalidExceptionHandler(InvalidException e) {
        return new RsData<>(e.statusCode, e.getMessage());
    }
}
