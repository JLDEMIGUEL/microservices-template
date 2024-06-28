package com.jldemiguel.microservice1.service;

import com.jldemiguel.microservice1.model.Product;
import com.jldemiguel.microservice1.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductsService {

    private final ProductsRepository repository;

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    public Product getProductById(UUID id) {
        return repository.findById(id).orElse(Product.builder().build());
    }
}
