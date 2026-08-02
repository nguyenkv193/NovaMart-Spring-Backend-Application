package com.novamart.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BAD_REQUEST("BAD_REQUEST", "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("UNAUTHORIZED", "Yêu cầu xác thực", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "Từ chối truy cập", HttpStatus.FORBIDDEN),
    NOT_FOUND("NOT_FOUND", "Không tìm thấy tài nguyên", HttpStatus.NOT_FOUND),
    CONFLICT("CONFLICT", "Xung đột trạng thái hoặc dữ liệu", HttpStatus.CONFLICT),
    INTERNAL_ERROR("INTERNAL_ERROR", "Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR);


    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;

    ErrorCode(String code, String defaultMessage, HttpStatus status) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
