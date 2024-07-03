package com.jldemiguel.microservice2.service;

import com.jldemiguel.microservice2.client.ProductClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductClient client;

    public void checkIfProductExists(UUID id) {
        log.info("Checking if product exists: " + id);
        client.getProductById(id);
    }
}
