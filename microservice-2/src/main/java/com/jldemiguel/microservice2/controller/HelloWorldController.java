package com.jldemiguel.microservice2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/microservice-2")
@RestController
public class HelloWorldController {

    @GetMapping("/hello-world")
    public ResponseEntity<String> helloWorld(){
        return ResponseEntity.ok("hello-world");
    }
}
