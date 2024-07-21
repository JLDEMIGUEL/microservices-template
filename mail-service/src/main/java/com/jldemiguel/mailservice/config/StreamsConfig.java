package com.jldemiguel.mailservice.config;

import com.jldemiguel.mailservice.model.UserOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class StreamsConfig {

    @Bean
    public Consumer<UserOrder> placeOrder() {
        return userOrder -> log.info("Sending email {}", userOrder); //TODO
    }
}
