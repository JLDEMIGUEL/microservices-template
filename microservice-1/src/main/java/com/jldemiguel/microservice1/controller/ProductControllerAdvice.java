package com.jldemiguel.microservice1.controller;

import com.jldemiguel.microservice1.model.response.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ProductControllerAdvice {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(Exception e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .reason(e.getMessage())
                .build();
        log.error("Exception while processing request: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
