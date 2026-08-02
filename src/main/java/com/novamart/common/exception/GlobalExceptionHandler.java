package com.novamart.common.exception;

import com.novamart.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException businessException, HttpServletRequest request
    ) {
        final ErrorCode errorCode = businessException.getErrorCode();
        log.warn("Business exception code={} path={}", errorCode.getCode(), request.getRequestURI());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode.getCode(), businessException.getMessage(), request.getRequestURI()));

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        final Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus())
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getDefaultMessage(), request.getRequestURI(), errors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Access denied for path={} reason={}",
                request.getRequestURI(),
                exception.getClass().getSimpleName()
        );

        return ResponseEntity
                .status(ErrorCode.FORBIDDEN.getStatus())
                .body(ErrorResponse.of(
                        ErrorCode.FORBIDDEN.getCode(),
                        ErrorCode.FORBIDDEN.getDefaultMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                "Unhandled exception for method={} path={}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(ErrorResponse.of(
                        ErrorCode.INTERNAL_ERROR.getCode(),
                        ErrorCode.INTERNAL_ERROR.getDefaultMessage(),
                        request.getRequestURI()
                ));
    }
}
