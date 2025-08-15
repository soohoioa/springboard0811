package com.project.board0811.common.exception;

import com.project.board0811.common.response.CommonApiResponse;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    // ==== 공통 유틸 ====
    private <T> ResponseEntity<CommonApiResponse<T>> build(BaseErrorCode code, T body) {
        return ResponseEntity.status(code.getStatus()).body(CommonApiResponse.fail(code, body));
    }
    private ResponseEntity<CommonApiResponse<Void>> build(BaseErrorCode code) {
        return ResponseEntity.status(code.getStatus()).body(CommonApiResponse.fail(code));
    }

    // ==== 비즈니스 커스텀 예외 ====
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleCustomException(CustomException e) {
        BaseErrorCode ec = e.getErrorCode();
        // detail message가 있을 수 있으므로 로깅에 실제 e.getMessage() 출력
        log.error("[CustomException] {} - {}", ec.getCode(), e.getMessage());
        return build(ec);
    }

    // ==== 인증/인가 ====
//    @ExceptionHandler(ExpiredJwtException.class)
//    public ResponseEntity<CommonApiResponse<Void>> handleExpiredJwtException(ExpiredJwtException e) {
//        log.error("[ExpiredJwtException] {}", e.getMessage());
//        return build(ErrorCode.JWT_EXPIRED);
//    }
//
//    @ExceptionHandler(JwtException.class)
//    public ResponseEntity<CommonApiResponse<Void>> handleJwtException(JwtException e) {
//        log.error("[JwtException] {}", e.getMessage());
//        return build(ErrorCode.JWT_INVALID);
//    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        log.error("[AccessDeniedException] {}", e.getMessage());
        return build(ErrorCode.FORBIDDEN);
    }

    // ==== Validation 계열 ====
    // @RequestBody DTO @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (a, b) -> a, // 동일 필드 다중 에러 시 최초 메시지 유지
                        LinkedHashMap::new
                ));

        String firstMessage = fieldErrors.values().stream().findFirst().orElse(ErrorCode.VALIDATION_ERROR.getMessage());
        log.error("[ValidationException] {} - {}", firstMessage, fieldErrors);
        return build(ErrorCode.VALIDATION_ERROR, fieldErrors);
    }

    // @ModelAttribute 바인딩 실패
    @ExceptionHandler(BindException.class)
    public ResponseEntity<CommonApiResponse<Map<String, String>>> handleBindException(BindException e) {
        Map<String, String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        log.error("[BindException] {}", fieldErrors);
        return build(ErrorCode.VALIDATION_ERROR, fieldErrors);
    }

    // @Validated 메서드 파라미터 제약 위반
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonApiResponse<Map<String, String>>> handleConstraintViolation(ConstraintViolationException e) {
        Map<String, String> violations = e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        log.error("[ConstraintViolationException] {}", violations);
        return build(ErrorCode.VALIDATION_ERROR, violations);
    }

    // ==== 요청 포맷/파라미터 ====
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleParsingException(HttpMessageNotReadableException e) {
        log.error("[HttpMessageNotReadableException] {}", e.getMessage());
        return build(ErrorCode.INVALID_JSON);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        log.error("[MissingServletRequestParameterException] {}", e.getMessage());
        return build(ErrorCode.MISSING_PARAMETER);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("[MethodArgumentTypeMismatchException] {}", e.getMessage());
        return build(ErrorCode.TYPE_MISMATCH);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        log.error("[HttpRequestMethodNotSupportedException] {}", e.getMessage());
        return build(ErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleNoHandlerFound(NoHandlerFoundException e) {
        // 활성화 필요:
        // spring.mvc.throw-exception-if-no-handler-found=true
        // spring.web.resources.add-mappings=false
        log.error("[NoHandlerFoundException] {}", e.getRequestURL());
        return build(ErrorCode.NOT_FOUND);
    }

    // ==== DB/데이터 계열 ====
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("[DataIntegrityViolationException] {}", e.getMessage(), e);
        return build(ErrorCode.DATA_INTEGRITY_VIOLATION);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleSQLIntegrityConstraintViolation(SQLIntegrityConstraintViolationException e) {
        log.error("[SQLIntegrityConstraintViolationException] {}", e.getMessage());
        return build(ErrorCode.DUPLICATE_VALUE);
    }

    // ==== 기타 ====
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("[IllegalArgumentException] {}", e.getMessage());
        return build(ErrorCode.INVALID_ARGUMENT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonApiResponse<Void>> handleException(Exception e) {
        log.error("[Exception] {}", e.getMessage(), e);
        return build(ErrorCode.INTERNAL_SERVER_ERROR);
    }


}
