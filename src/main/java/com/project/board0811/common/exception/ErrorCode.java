package com.project.board0811.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseErrorCode {

    // ===== 공통 =====
    INTERNAL_SERVER_ERROR("COMMON-500", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_JSON("COMMON-400-JSON", "요청 형식이 잘못되었습니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("COMMON-400-VALIDATION", "입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER("COMMON-400-MISSING_PARAM", "필수 요청 파라미터가 누락되었습니다.", HttpStatus.BAD_REQUEST),
    TYPE_MISMATCH("COMMON-400-TYPE_MISMATCH", "요청 파라미터 타입이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED("COMMON-405", "지원하지 않는 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    NOT_FOUND("COMMON-404", "요청하신 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_ARGUMENT("COMMON-400-ARG", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),

    // ===== 인증/인가 =====
    UNAUTHORIZED("AUTH-401", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("AUTH-403", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    JWT_EXPIRED("AUTH-401-JWT_EXPIRED", "엑세스 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    JWT_INVALID("AUTH-401-JWT_INVALID", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),

    // ===== 데이터/DB =====
    DATA_INTEGRITY_VIOLATION("DB-409-INTEGRITY", "데이터 무결성 제약조건을 위반했습니다.", HttpStatus.CONFLICT),
    DUPLICATE_VALUE("DB-400-DUPLICATE", "이미 처리된 요청입니다.", HttpStatus.BAD_REQUEST),

    // ===== 도메인 예시 =====
    USER_NOT_FOUND("USER-404", "해당 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    BOARD_NOT_FOUND("BOARD-404", "게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;

    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public HttpStatus getStatus() { return status; }
}
