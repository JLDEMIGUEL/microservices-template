package com.jldemiguel.microservice2.service;

import com.jldemiguel.microservice2.model.jpa.Order;
import com.jldemiguel.microservice2.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final ProductService service;
    private final MailService mailService;


    public Mono<Order> placeOrder(Order order) {
        log.info("Placing order: {} for user: {}", order.getProductId(), order.getUserId());
        return Mono.just(order)
                .flatMap(o -> service.getProductById(o.getProductId())
                        .flatMap(product -> repository.save(o)
                                .flatMap(savedOrder ->
                                        mailService.sendEmail(product)
                                                .then(Mono.just(savedOrder))
                                )
                        )
                );
    }

    public Flux<Order> getUserOrders(UUID userId) {
        return repository.findAllByUserIdOrderByCreatedDateDesc(userId);
    }
}
