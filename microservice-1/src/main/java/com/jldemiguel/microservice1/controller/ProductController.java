package com.jldemiguel.microservice1.controller;

import com.jldemiguel.microservice1.model.jpa.Product;
import com.jldemiguel.microservice1.service.ProductsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductsService service;

    @GetMapping("")
    public ResponseEntity<Page<Product>> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        log.info("Getting all products");
        return ResponseEntity.ok(service.getAllProducts(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
        log.info("Getting product by id: " + id);
        return ResponseEntity.ok(service.getProductById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        log.info("Creating product: " + product);
        Product savedProduct = service.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }
}
