package com.jldemiguel.mailservice.handler;

import com.jldemiguel.mailservice.model.UserDto;
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
public class NewAccountMailHandler {

    private static final String NEW_ACCOUNT_SUBJECT = "Welcome %s";
    private static final String NEW_ACCOUNT_EMAIL_TEMPLATE = "newAccountEmail";

    private final MailService mailService;
    private final TemplateEngine templateEngine;


    public void sendNewAccountEmail(UserDto user) {
        log.info("Sending new account email {}", user);
        Context context = new Context();
        context.setVariables(Map.of("username", user.getUsername(), "email", user.getEmail()));
        String emailText = templateEngine.process(NEW_ACCOUNT_EMAIL_TEMPLATE, context);
        mailService.sendEmail(user.getEmail(), NEW_ACCOUNT_SUBJECT.formatted(user.getUsername()), emailText);
    }
}
