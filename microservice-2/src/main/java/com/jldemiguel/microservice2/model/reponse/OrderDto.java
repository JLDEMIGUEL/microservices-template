package com.jldemiguel.microservice2.model.reponse;

import com.jldemiguel.microservice2.model.Product;
import com.jldemiguel.microservice2.model.jpa.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private UUID id;
    private String name;
    private Double price;
    private Instant createdDate;

    public static OrderDto from(Order order, Product product) {
        return OrderDto.builder()
                .id(order.getId())
                .name(product.getName())
                .price(product.getPrice())
                .createdDate(order.getCreatedDate())
                .build();
    }
}
