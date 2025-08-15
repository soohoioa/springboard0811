package com.project.board0811.common.exception;

import com.project.board0811.common.response.CommonApiResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${server.error.path:/error}")
public class ApiErrorController implements ErrorController {

    @RequestMapping
    public ResponseEntity<CommonApiResponse<Void>> handleError(HttpServletRequest request) {
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object requestUriObj = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object exceptionObj = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int statusCode = (statusObj instanceof Integer) ? (Integer) statusObj
                : HttpStatus.INTERNAL_SERVER_ERROR.value();

        String path = requestUriObj != null ? requestUriObj.toString() : "N/A";

        ErrorCode code = mapStatusToErrorCode(statusCode);

        // 로그에는 실제 상태와 경로/예외 정보를 남김
        if (exceptionObj != null) {
            log.error("[/error] status={}, path={}, ex={}", statusCode, path, exceptionObj);
        } else {
            log.warn("[/error] status={}, path={}", statusCode, path);
        }

        return ResponseEntity.status(code.getStatus())
                .body(CommonApiResponse.fail(code));
    }

    private ErrorCode mapStatusToErrorCode(int statusCode) {
        return switch (statusCode) {
            case 400 -> ErrorCode.INVALID_ARGUMENT;
            case 401 -> ErrorCode.UNAUTHORIZED;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.NOT_FOUND;            // ✅ 404를 공통 포맷으로 변환
            case 405 -> ErrorCode.METHOD_NOT_ALLOWED;
            case 409 -> ErrorCode.DATA_INTEGRITY_VIOLATION;
            default -> ErrorCode.INTERNAL_SERVER_ERROR;
        };
    }
}
