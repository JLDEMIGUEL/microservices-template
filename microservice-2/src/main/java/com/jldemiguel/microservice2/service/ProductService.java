package com.jldemiguel.microservice2.service;

import com.jldemiguel.microservice2.client.ProductClient;
import com.jldemiguel.microservice2.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductClient client;

    public Mono<Product> getProductById(UUID id) {
        log.info("Getting product by id: " + id);
        return client.getProductById(id);
    }
}
