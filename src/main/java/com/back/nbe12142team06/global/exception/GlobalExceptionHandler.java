package com.back.nbe12142team06.global.exception;

import com.back.nbe12142team06.global.response.RsData;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {


    // DTO로 변환은 가능하지만 DTO 검증에서 걸리는 경우 예시로 @NotBlank 위반
    @ExceptionHandler
    public RsData<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));

        return new RsData<>("400-1", msg);
    }

    // DTO로 변환 자체가 불가할때 예시로 우리 role에는 없는 "role": "GG" 같은 요청
    @ExceptionHandler
    public RsData<?> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException e) {
        return new RsData<>("400-2", "요청 본문의 형식이 올바르지 않습니다.");
    }

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
