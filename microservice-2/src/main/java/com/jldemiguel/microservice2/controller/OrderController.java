package com.jldemiguel.microservice2.controller;

import com.jldemiguel.microservice2.model.jpa.Order;
import com.jldemiguel.microservice2.model.reponse.OrderDto;
import com.jldemiguel.microservice2.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @GetMapping("")
    public ResponseEntity<Flux<OrderDto>> getUserProducts(Principal principal) {
        log.info("Getting orders for user: " + principal.getName());
        return ResponseEntity.ok(service.getUserOrders(UUID.fromString(principal.getName())));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Mono<OrderDto>> placeOrder(Principal principal, @PathVariable UUID productId) {
        log.info("Placing order for user: " + principal.getName() + " for product: " + productId);
        Mono<OrderDto> order = service.placeOrder(Order.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .userId(UUID.fromString(principal.getName()))
                .isNew(true)
                .build());
        return ResponseEntity.ok(order);
    }
}
