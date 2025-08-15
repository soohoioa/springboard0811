package com.project.board0811.common.exception;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
    String getCode();       // 에러 코드 문자열
    String getMessage();    // 에러 메시지
    HttpStatus getStatus(); // HTTP 상태
}
