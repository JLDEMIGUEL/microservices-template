package com.jldemiguel.microservice2.service;

import com.jldemiguel.microservice2.model.jpa.Order;
import com.jldemiguel.microservice2.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final ProductService service;

    public Order placeOrder(Order order) {
        service.getProductById(order.getProductId());
        return repository.save(order);
    }

    public List<Order> getUserOrders(UUID userId) {
        return repository.findAllByUserIdOrderByCreatedDateDesc(userId);
    }
}
