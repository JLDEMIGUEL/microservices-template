package com.jldemiguel.microservice2.service;

import com.jldemiguel.microservice2.model.Product;
import com.jldemiguel.microservice2.model.jpa.Order;
import com.jldemiguel.microservice2.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final ProductService service;
    private final MailService mailService;

    public Order placeOrder(Order order) {
        Product product = service.getProductById(order.getProductId());
        log.info("Placing order: " + order.getProductId() + " for user: " + order.getUserId());
        Order savedOrder = repository.save(order);
        mailService.sendEmail(product);
        return savedOrder;
    }

    public List<Order> getUserOrders(UUID userId) {
        return repository.findAllByUserIdOrderByCreatedDateDesc(userId);
    }
}
