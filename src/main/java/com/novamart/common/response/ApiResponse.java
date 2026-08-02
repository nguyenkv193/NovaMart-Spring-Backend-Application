package com.novamart.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int code;
    private String message;

    private T data;
    private Map<String, String> errors;

    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>(
                code,
                message,
                data,
                null,
                LocalDateTime.now()
        );
    }

    public static <T> ApiResponse<T> failure(int code, String message, Map<String, String> errors) {
        return new ApiResponse<>(
                code,
                message,
                null,
                errors,
                LocalDateTime.now()
        );
    }
}
