package com.jldemiguel.microservice1.repository;

import com.jldemiguel.microservice1.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductsRepository extends JpaRepository<Product, UUID> {
}
