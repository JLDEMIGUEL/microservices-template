package com.jldemiguel.microservice2.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jldemiguel.microservice2.model.Product;
import com.jldemiguel.microservice2.model.reponse.ErrorResponse;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.UUID;


@FeignClient(value = "microservice-1",
        path = "/product",
        configuration = ProductClient.Config.class)
public interface ProductClient {

    @GetMapping("/{id}")
    Product getProductById(@PathVariable UUID id);

    class Config {

        @Bean
        public RequestInterceptor requestInterceptor() {
            return request -> {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getCredentials() instanceof Jwt jwt) {
                    request.header("Authorization", "Bearer " + jwt.getTokenValue());
                }
            };
        }

        @Bean
        public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
            return (methodKey, response) -> {
                try {
                    ErrorResponse errorResponse =
                            objectMapper.readValue(response.body().asInputStream(), ErrorResponse.class);
                    throw new RuntimeException("Error when calling " + methodKey + ". Error: " +
                            errorResponse.getReason());
                } catch (IOException e) {
                    throw new RuntimeException("Error when calling " + methodKey + ".");
                }
            };
        }
    }
}
