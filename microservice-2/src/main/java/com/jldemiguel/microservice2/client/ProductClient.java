package com.jldemiguel.microservice2.client;

import com.jldemiguel.microservice2.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
public class ProductClient {

    private final WebClient webClient;

    public ProductClient(@Value("${microservice-1.url}") String baseUrl, @Qualifier("lbWebClient") WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(baseUrl + "/product").build();
    }

    public Mono<Product> getProductById(UUID id) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> webClient.get()
                        .uri("/{id}", id)
                        .headers(headers -> {
                            log.info("Adding authorization header to request to microservice-1");
                            if (authentication != null && authentication.getCredentials() instanceof Jwt jwt) {
                                headers.setBearerAuth(jwt.getTokenValue());
                            }
                        })
                        .retrieve()
                        .bodyToMono(Product.class))
                .onErrorResume(e -> {
                    log.error("Error when calling getProductById: " + e.getMessage(), e);
                    return Mono.error(new RuntimeException("Error when calling getProductById."));
                });
    }
}