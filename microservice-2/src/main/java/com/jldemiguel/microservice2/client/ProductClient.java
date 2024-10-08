package com.jldemiguel.microservice2.client;

import com.jldemiguel.microservice2.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.jldemiguel.microservice2.config.CacheConfig.CACHE_NAME;

@Slf4j
@Service
public class ProductClient {

    private final WebClient webClient;

    public ProductClient(@Value("${microservice-1.url}") String baseUrl,
                         @Qualifier("lbWebClient") WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(baseUrl + "/product").build();
    }

    @Cacheable(value = CACHE_NAME, key = "#id")
    public Mono<Product> getProductById(UUID id) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> webClient.get()
                        .uri("/{id}", id)
                        .retrieve()
                        .bodyToMono(Product.class))
                .onErrorResume(e -> {
                    log.error("Error when calling getProductById: " + e.getMessage(), e);
                    return Mono.error(e);
                });
    }
}