package com.jldemiguel.authserver.service;

import com.jldemiguel.authserver.model.UserDto;
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
    public static final String NEW_ACCOUNT_ROUTING_KEY = "new-account";
    public static final String SEND_EMAIL_EXCHANGE = "sendEmail";

    private final StreamBridge streamBridge;

    public void sendEmail(UserDto user) {
        log.info("Sending email {}", user);
        streamBridge.send(SEND_EMAIL_EXCHANGE, MessageBuilder.withPayload(user)
                .setHeader(ROUTING_KEY_HEADER, NEW_ACCOUNT_ROUTING_KEY)
                .build());
    }
}