package com.jldemiguel.microservice2.service;

import com.jldemiguel.microservice2.model.Product;
import com.jldemiguel.microservice2.model.UserOrder;
import com.jldemiguel.microservice2.model.jpa.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    public static final String ROUTING_KEY_HEADER = "routingKey";
    public static final String PLACE_ORDER_ROUTING_KEY = "place-order";
    public static final String SEND_EMAIL_EXCHANGE = "sendEmail";

    private final StreamBridge streamBridge;

    public void sendEmail(Order order, Product product) {
        UserOrder userOrder = UserOrder.builder()
                .userId(order.getUserId())
                .price(product.getPrice())
                .product(product.getName())
                .build();
        log.info("Sending email {}", userOrder);
        streamBridge.send(SEND_EMAIL_EXCHANGE, MessageBuilder.withPayload(userOrder)
                .setHeader(ROUTING_KEY_HEADER, PLACE_ORDER_ROUTING_KEY)
                .build());
    }
}
