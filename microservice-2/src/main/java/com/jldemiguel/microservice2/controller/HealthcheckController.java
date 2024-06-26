package com.jldemiguel.microservice2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RequestMapping("/microservice-2")
@RestController
public class HealthcheckController {

    @GetMapping("/healthcheck")
    public ResponseEntity<Map<String, Object>> helloWorld(){
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
