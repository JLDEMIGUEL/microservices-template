package com.jldemiguel.mailservice.config;

import com.jldemiguel.mailservice.handler.NewAccountMailHandler;
import com.jldemiguel.mailservice.handler.PlaceOrderMailHandler;
import com.jldemiguel.mailservice.model.UserDto;
import com.jldemiguel.mailservice.model.UserOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class StreamsConfig {

    @Bean
    public Consumer<UserDto> newAccount(NewAccountMailHandler handler) {
        return handler::sendNewAccountEmail;
    }

    @Bean
    public Consumer<UserOrder> placeOrder(PlaceOrderMailHandler handler) {
        return handler::sendPlaceOrderEmail;
    }
}
