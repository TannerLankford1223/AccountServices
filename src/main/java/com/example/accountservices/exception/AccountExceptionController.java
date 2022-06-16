package com.example.accountservices.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class AccountExceptionController extends ResponseEntityExceptionHandler {

    // Handles annotation validation errors
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {

        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return getExceptionResponse(HttpStatus.BAD_REQUEST, request,
                validationErrors);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        return getExceptionResponse(HttpStatus.BAD_REQUEST, request, Collections.singletonList(ex.getLocalizedMessage()));
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<String> validationErrors = ex.getConstraintViolations().stream()
                .map(violation ->
                        violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        return getExceptionResponse(HttpStatus.BAD_REQUEST, request, validationErrors);
    }

    private ResponseEntity<Object> getExceptionResponse(HttpStatus status, WebRequest request, List<String> messages) {
        Map<String, Object> responseBody = new LinkedHashMap<>();
        String path = ((ServletWebRequest) request).getRequest().getRequestURI().toString();
        responseBody.put("timestamp", Instant.now());
        responseBody.put("status", status.value());
        responseBody.put("error", status.getReasonPhrase());
        responseBody.put("message", messages);
        responseBody.put("path", path);

        return new ResponseEntity<>(responseBody, status);
    }

}

