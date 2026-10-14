package com.vit.parkingappeal.web;

import com.vit.parkingappeal.service.InvalidStateException;
import com.vit.parkingappeal.service.NotFoundException;
import com.vit.parkingappeal.service.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Turns service exceptions into clean JSON errors for the REST API only. */
@RestControllerAdvice(assignableTypes = {ApiController.class, TestSupportController.class})
public class ApiExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> validation(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("errors", e.getErrors()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<Map<String, Object>> conflict(InvalidStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }
}
