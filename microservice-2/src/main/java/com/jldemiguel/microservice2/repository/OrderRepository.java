package com.jldemiguel.microservice2.repository;

import com.jldemiguel.microservice2.model.jpa.Order;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface OrderRepository extends R2dbcRepository<Order, UUID> {

    Flux<Order> findAllByUserIdOrderByCreatedDateDesc(UUID userId);
}
