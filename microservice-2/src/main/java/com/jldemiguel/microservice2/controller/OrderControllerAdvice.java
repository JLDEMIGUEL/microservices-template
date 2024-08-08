package com.jldemiguel.microservice2.controller;


import com.jldemiguel.microservice2.model.reponse.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@ControllerAdvice
public class OrderControllerAdvice {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(Exception e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .reason(e.getMessage())
                .build();
        log.error("Exception while processing request: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientResponseException(WebClientResponseException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .reason(e.getMessage())
                .build();
        log.error("Exception while processing request: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
