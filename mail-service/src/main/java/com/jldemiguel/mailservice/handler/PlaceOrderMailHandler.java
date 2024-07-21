package com.jldemiguel.mailservice.handler;

import com.jldemiguel.mailservice.model.UserOrder;
import com.jldemiguel.mailservice.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceOrderMailHandler {

    private static final String PLACE_ORDER_SUBJECT = "You have placed a new order!";
    private static final String PLACE_ORDER_EMAIL_TEMPLATE = "placeOrderEmail";

    private final MailService mailService;
    private final TemplateEngine templateEngine;


    public void sendPlaceOrderEmail(UserOrder userOrder) {
        log.info("Sending place order email {}", userOrder);
        Context context = new Context();
        context.setVariables(Map.of("product", userOrder.getProduct(), "price", userOrder.getPrice()));
        String emailText = templateEngine.process(PLACE_ORDER_EMAIL_TEMPLATE, context);
        mailService.sendEmail(userOrder.getEmail(), PLACE_ORDER_SUBJECT, emailText);
    }
}
