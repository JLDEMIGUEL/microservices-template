package com.jldemiguel.microservice1.controller;

import com.jldemiguel.microservice1.model.jpa.Product;
import com.jldemiguel.microservice1.service.ProductsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductsService service;

    @GetMapping("")
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("Getting all products");
        return ResponseEntity.ok(service.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
        log.info("Getting product by id: " + id);
        return ResponseEntity.ok(service.getProductById(id));
    }
}
