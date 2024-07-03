package com.jldemiguel.microservice2.service;

import com.jldemiguel.microservice2.client.ProductClient;
import com.jldemiguel.microservice2.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductClient client;

    public Product getProductById(UUID id) {
        return client.getProductById(id);
    }
}
