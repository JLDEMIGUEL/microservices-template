package com.jldemiguel.microservice2.service;

import com.jldemiguel.microservice2.model.Product;
import com.jldemiguel.microservice2.model.UserOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    public static final String ROUTING_KEY_HEADER = "routingKey";
    public static final String PLACE_ORDER_ROUTING_KEY = "place-order";
    public static final String SEND_EMAIL_EXCHANGE = "sendEmail";

    private final StreamBridge streamBridge;

    public void sendEmail(Product product) {
        UserOrder userOrder = UserOrder.builder()
                .email(getEmail())
                .price(product.getPrice())
                .product(product.getName())
                .build();
        log.info("Sending email {}", userOrder);
        streamBridge.send(SEND_EMAIL_EXCHANGE, MessageBuilder.withPayload(userOrder)
                .setHeader(ROUTING_KEY_HEADER, PLACE_ORDER_ROUTING_KEY)
                .build());
    }

    private String getEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("email");
        } else {
            throw new RuntimeException("User not authenticated");
        }
    }
}
