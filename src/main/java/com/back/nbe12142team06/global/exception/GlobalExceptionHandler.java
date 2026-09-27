package com.back.nbe12142team06.global.exception;

import com.back.nbe12142team06.global.response.RsData;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;

@Slf4j
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
        log.warn("[{}] {}", e.statusCode,e.getMessage());
        return new RsData<>(e.statusCode, e.getMessage());
    }

    @ExceptionHandler
    public RsData<?> invalidExceptionHandler(InvalidException e) {
        return new RsData<>(e.statusCode, e.getMessage());
    }

    @ExceptionHandler
    public RsData<?> internalServerErrorExceptionHandler(InternalServerErrorException e) {
        return new RsData<>(e.statusCode, e.getMessage());
    }

    @ExceptionHandler
    public RsData<?> unAuthorizedExceptionHandler(UnauthorizedException e) {
        return new RsData<>(e.statusCode, e.getMessage());
    }

    @ExceptionHandler
    public RsData<?> ForbiddenExceptionHandler(ForbiddenException e) {
        return new RsData<>(e.statusCode, e.getMessage());
    }

    // 스프링이 던지는 "요청 자체가 잘못된" 예외들. 아래 Exception 핸들러가 가로채면 사용자 실수가 500 으로 나가서 따로 처리한다.

    // GET /api/v1/posts/abc (숫자 자리에 문자), ?page=abc
    @ExceptionHandler
    public RsData<?> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException e) {
        log.warn("[400-4] 요청 파라미터 타입 오류: {}", e.getName());
        return new RsData<>("400-4", "요청 파라미터의 형식이 올바르지 않습니다.");
    }

    // 필수 쿼리 파라미터 누락
    @ExceptionHandler
    public RsData<?> missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException e) {
        log.warn("[400-5] 필수 요청 파라미터 누락: {}", e.getParameterName());
        return new RsData<>("400-5", "필수 요청 파라미터가 없습니다: " + e.getParameterName());
    }

    // 없는 URL 호출
    @ExceptionHandler
    public RsData<?> noResourceFoundExceptionHandler(NoResourceFoundException e) {
        log.warn("[404] 존재하지 않는 경로: {}", e.getResourcePath());
        return new RsData<>("404", "존재하지 않는 경로입니다.");
    }

    // 지원하지 않는 HTTP 메서드 (GET 만 되는 곳에 DELETE 등)
    @ExceptionHandler
    public RsData<?> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException e) {
        log.warn("[405] 지원하지 않는 HTTP 메서드: {}", e.getMethod());
        return new RsData<>("405", "지원하지 않는 HTTP 메서드입니다.");
    }

    @ExceptionHandler
    public RsData<?> exceptionHandler(Exception e) {
        log.error("처리되지 않은 예외 발생", e);
        return new RsData<>("500-1", "서버 오류가 발생했습니다.");
    }
}
