package com.jldemiguel.microservice2.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.jldemiguel.microservice2.model.UserOrder;
import com.jldemiguel.microservice2.model.jpa.Order;
import com.jldemiguel.microservice2.model.reponse.ErrorResponse;
import com.jldemiguel.microservice2.repository.OrderRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.jldemiguel.microservice2.integration.OrderControllerIntegrationTest.PORT;
import static com.jldemiguel.microservice2.service.MailService.PLACE_ORDER_ROUTING_KEY;
import static com.jldemiguel.microservice2.service.MailService.ROUTING_KEY_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestChannelBinderConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"microservice-1.url=http://localhost:" + PORT}
)
public class OrderControllerIntegrationTest {

    public static final String USER_ID = "d0ab9326-1772-4a35-bc2b-46261fcea6ad";
    public static final int PORT = 8000;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private OutputDestination output;

    @Autowired
    private ObjectMapper objectMapper;

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void setUpAll() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(PORT));
        wireMockServer.start();
    }

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("provideControllerPaths")
    void shouldReturn401_whenUnauthorizedRequest(String path) throws Exception {
        mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
    }

    public static Stream<Arguments> provideControllerPaths() {
        return Stream.of(
                Arguments.of("/order", "/order/id")
        );
    }

    @Test
    @WithMockUser(value = USER_ID)
    void shouldGetAllOrders_whenGetAllOrdersUrlIsCalled() throws Exception {
        // given
        List<Order> savedOrders = List.of(
                Order.builder().productId(UUID.randomUUID()).userId(UUID.fromString(USER_ID)).build(),
                Order.builder().productId(UUID.randomUUID()).userId(UUID.fromString(USER_ID)).build(),
                Order.builder().productId(UUID.randomUUID()).userId(UUID.fromString(USER_ID)).build()
        );
        orderRepository.saveAll(savedOrders);

        // when
        MvcResult result = mockMvc.perform(get("/order")
                        .with(jwt().jwt((jwt) -> jwt.claim("sub", USER_ID))))
                .andExpect(status().isOk())
                .andReturn();

        // then
        Order[] orders = objectMapper.readValue(result.getResponse().getContentAsString(), Order[].class);

        assertThat(orders).hasSize(3);
        assertThat(orders).allMatch(order -> USER_ID.equals(order.getUserId().toString()));
    }

    @Test
    void shouldReturnErrorMessage_whenInvalidSubjectInJwt() throws Exception {
        // when
        MvcResult result = mockMvc.perform(get("/order")
                        .with(jwt().jwt((jwt) -> jwt.claim("sub", "invalid"))))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        ErrorResponse errorResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);

        assertThat(errorResponse.getReason()).contains("Invalid UUID string");
    }

    @Test
    @WithMockUser(value = USER_ID)
    void shouldSaveOrderAndSendEmail_whenSaveOrderUrlIsCalledAndValidProduct() throws Exception {
        // given
        UUID randomUUID = UUID.randomUUID();
        stubForProductWithId(randomUUID);

        // when
        MvcResult result = mockMvc.perform(post("/order/" + randomUUID)
                        .with(jwt()
                                .jwt((jwt) -> jwt
                                        .claim("sub", USER_ID)
                                        .claim("email", "test"))
                        ))
                .andExpect(status().isOk())
                .andReturn();

        // then
        Order order = objectMapper.readValue(result.getResponse().getContentAsString(), Order.class);
        assertThat(order.getUserId().toString()).isEqualTo(USER_ID);
        assertThat(order.getProductId().toString()).isEqualTo(randomUUID.toString());

        Order dbOrder = orderRepository.findAllByUserIdOrderByCreatedDateDesc(UUID.fromString(USER_ID)).get(0);
        assertThat(dbOrder.getProductId()).isEqualTo(randomUUID);

        Message<?> message = output.receive(1000L, "sendEmail");
        assertThat(message.getHeaders().get(ROUTING_KEY_HEADER)).isEqualTo(PLACE_ORDER_ROUTING_KEY);
        UserOrder userOrder = objectMapper.readValue(new String((byte[]) message.getPayload(), StandardCharsets.UTF_8), UserOrder.class);
        assertThat(userOrder.getEmail()).isEqualTo("test");
        assertThat(userOrder.getPrice()).isEqualTo(100.0);
        assertThat(userOrder.getProduct()).isEqualTo("Product 1");
    }

    private static void stubForProductWithId(UUID randomUUID) {
        wireMockServer.stubFor(WireMock.get("/product/" + randomUUID)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "%s",
                                    "name": "Product 1",
                                    "price": 100.0
                                }
                                """.formatted(randomUUID))));
    }
}
