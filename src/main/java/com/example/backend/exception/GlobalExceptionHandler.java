package com.example.backend.exception;

import com.example.backend.api.CommonApiResponse;
import com.example.backend.api.CommonApiResponseFactory;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final CommonApiResponseFactory responseFactory;

    public GlobalExceptionHandler(CommonApiResponseFactory responseFactory) {
        this.responseFactory = responseFactory;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleNotFound(ResourceNotFoundException e, Locale locale) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(responseFactory.failure("NOT_FOUND", "error.not_found", locale));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleValidation(MethodArgumentNotValidException e, Locale locale) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));
        String base = responseFactory.failure("VALIDATION_ERROR", "error.validation", locale).message();
        return ResponseEntity.badRequest()
                .body(CommonApiResponse.createFailure("VALIDATION_ERROR", base + " (" + message + ")"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CommonApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e, Locale locale) {
        String base = responseFactory.failure("VALIDATION_ERROR", "error.validation", locale).message();
        return ResponseEntity.badRequest()
                .body(CommonApiResponse.createFailure("VALIDATION_ERROR", base + " (" + e.getMessage() + ")"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonApiResponse<Void>> handleUnknown(Exception e, Locale locale) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(responseFactory.failure("INTERNAL_SERVER_ERROR", "error.internal", locale));
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
