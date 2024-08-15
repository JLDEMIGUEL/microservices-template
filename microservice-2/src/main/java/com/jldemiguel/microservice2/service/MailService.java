package com.jldemiguel.microservice2.service;

import com.jldemiguel.microservice2.model.Product;
import com.jldemiguel.microservice2.model.UserOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    public static final String ROUTING_KEY_HEADER = "routingKey";
    public static final String PLACE_ORDER_ROUTING_KEY = "place-order";
    public static final String SEND_EMAIL_EXCHANGE = "sendEmail";

    private final StreamBridge streamBridge;

    public Mono<Void> sendEmail(Product product) {
        return getEmail()
                .doOnNext(email -> {
                    UserOrder userOrder = UserOrder.builder()
                            .email((String) email)
                            .price(product.getPrice())
                            .product(product.getName())
                            .build();
                    log.info("Sending email {}", userOrder);
                    streamBridge.send(SEND_EMAIL_EXCHANGE, MessageBuilder.withPayload(userOrder)
                            .setHeader(ROUTING_KEY_HEADER, PLACE_ORDER_ROUTING_KEY)
                            .build());
                }).then()
                .onErrorMap(e -> new IllegalArgumentException("No email found in the request"));
    }

    private Mono<Object> getEmail() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof Jwt)
                .map(principal -> ((Jwt) principal).getClaim("email"))
                .switchIfEmpty(Mono.error(new RuntimeException()));
    }
}
