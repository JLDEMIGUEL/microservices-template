package com.jldemiguel.microservice2.controller;

import com.jldemiguel.microservice2.model.jpa.Order;
import com.jldemiguel.microservice2.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @GetMapping("")
    public ResponseEntity<List<Order>> getUserProducts(Principal principal) {
        return ResponseEntity.ok(service.getUserOrders(UUID.fromString(principal.getName())));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Order> placeOrder(Principal principal, @PathVariable UUID productId) {
        Order order = service.placeOrder(Order.builder()
                .productId(productId)
                .userId(UUID.fromString(principal.getName()))
                .build());
        return ResponseEntity.ok(order);
    }
}
