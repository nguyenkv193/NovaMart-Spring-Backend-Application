package com.novamart.common.response;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {
    private String code;
    private String message;
    private String path;
    private Map<String, String> errors;
    private LocalDateTime timestamp;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String code, String message, String path, Map<String, String> errors) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }

    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(code, message, path, null);
    }

    public static ErrorResponse of(String code, String message, String path, Map<String, String> errors) {
        return new ErrorResponse(code, message, path, errors);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
