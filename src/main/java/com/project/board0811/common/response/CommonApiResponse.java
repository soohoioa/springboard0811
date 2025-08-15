package com.project.board0811.common.response;

import com.project.board0811.common.exception.BaseErrorCode;
import com.project.board0811.common.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommonApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final Integer status;     // HTTP status (숫자)
    private final T data;
    private final LocalDateTime timestamp;

    @Builder
    private CommonApiResponse(boolean success, String code, String message, Integer status, T data, LocalDateTime timestamp) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.status = status;
        this.data = data;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    // 성공 응답
    public static <T> CommonApiResponse<T> ok(T data) {
        return CommonApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message("요청이 성공적으로 처리되었습니다.")
                .status(200)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static CommonApiResponse<Void> ok() {
        return ok(null);
    }

    // 실패 응답 (ErrorCode)
    public static <T> CommonApiResponse<T> fail(BaseErrorCode errorCode) {
        return CommonApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .status(errorCode.getStatus().value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 실패 응답 (ErrorCode + data)
    public static <T> CommonApiResponse<T> fail(BaseErrorCode errorCode, T data) {
        return CommonApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .status(errorCode.getStatus().value())
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 실패 응답 (임의 코드/메시지)
    public static <T> CommonApiResponse<T> fail(String code, String message) {
        return CommonApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .status(ErrorCode.INVALID_ARGUMENT.getStatus().value())
                .timestamp(LocalDateTime.now())
                .build();
    }

}
