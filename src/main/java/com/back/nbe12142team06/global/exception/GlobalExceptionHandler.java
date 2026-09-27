package com.back.nbe12142team06.global.exception;

import com.back.nbe12142team06.global.response.RsData;
import org.springframework.dao.DataIntegrityViolationException;
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

    // 서비스 계층에서 미처 잡지 못한 DB 유니크 제약 위반이 여기까지 새어나왔을 때의
    // 최후 방어선. 동시 요청 경쟁 등으로 발생하며, 원문 메시지에는 제약 이름·테이블
    // 구조가 그대로 들어있어 그대로 응답에 담으면 내부 스키마가 노출된다.
    @ExceptionHandler
    public RsData<?> dataIntegrityViolationExceptionHandler(DataIntegrityViolationException e) {
        return new RsData<>("409", "이미 처리된 요청입니다.");
    }
}
