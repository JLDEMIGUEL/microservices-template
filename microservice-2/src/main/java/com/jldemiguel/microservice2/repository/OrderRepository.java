package com.jldemiguel.microservice2.repository;

import com.jldemiguel.microservice2.model.jpa.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findAllByUserIdOrderByCreatedDateDesc(UUID userId);
}
