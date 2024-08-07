package com.jldemiguel.mailservice.integration;

import com.jldemiguel.mailservice.model.UserDto;
import com.jldemiguel.mailservice.model.UserOrder;
import com.jldemiguel.mailservice.service.MailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestChannelBinderConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class MailServiceIntegrationTest {

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private InputDestination input;

    @MockBean
    private MailService mailService;

    @Test
    void shouldSendEmail_whenNewAccountIsCreated() {
        // given
        UserDto user = UserDto.builder()
                .username("test")
                .email("test@test.com")
                .build();

        //when
        input.send(MessageBuilder.withPayload(user).build(), "sendEmail.newAccount");

        //then
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);

        verify(mailService).sendEmail(toCaptor.capture(), subjectCaptor.capture(), any());
        assertEquals(user.getEmail(), toCaptor.getValue());
        assertEquals("Welcome test", subjectCaptor.getValue());
    }

    @Test
    void shouldSendEmail_whenOrderIsPlaced() {
        // given
        UserOrder order = UserOrder.builder()
                .email("test@test.com")
                .product("test")
                .price(1.0)
                .build();

        //when
        input.send(MessageBuilder.withPayload(order).build(), "sendEmail.placeOrder");

        //then
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);

        verify(mailService).sendEmail(toCaptor.capture(), subjectCaptor.capture(), any());
        assertEquals(order.getEmail(), toCaptor.getValue());
        assertEquals("You have placed a new order!", subjectCaptor.getValue());
    }
}
