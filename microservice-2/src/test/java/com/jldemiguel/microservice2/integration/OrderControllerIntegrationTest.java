package com.jldemiguel.microservice2.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.jldemiguel.microservice2.config.TestConfig;
import com.jldemiguel.microservice2.model.UserOrder;
import com.jldemiguel.microservice2.model.jpa.Order;
import com.jldemiguel.microservice2.model.reponse.ErrorResponse;
import com.jldemiguel.microservice2.repository.OrderRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static com.jldemiguel.microservice2.integration.OrderControllerIntegrationTest.PORT;
import static com.jldemiguel.microservice2.util.PollingUtils.poll;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@ActiveProfiles("test")
@Import({TestChannelBinderConfiguration.class})
@ContextConfiguration(classes = {TestConfig.class})
@AutoConfigureWebTestClient
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"microservice-1.url=http://localhost:" + PORT,
                "spring.main.allow-bean-definition-overriding=true"}
)
public class OrderControllerIntegrationTest {

    public static final String USER_ID = "d0ab9326-1772-4a35-bc2b-46261fcea6ad";
    public static final String EMAIL = "test@test.com";
    public static final SecurityMockServerConfigurers.JwtMutator JWT = mockJwt().jwt(jwt -> jwt
            .claim("sub", USER_ID).claim("email", EMAIL));

    public static final int PORT = 8000;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private OutputDestination output;

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void setUpAll() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(PORT));
        wireMockServer.start();
    }

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll().block();
        wireMockServer.resetAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/order", "/order/id"})
    void shouldReturn401_whenUnauthorizedRequest(String path) {
        webTestClient.get().uri(path).exchange().expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(value = USER_ID)
    void shouldGetAllOrders_whenGetAllOrdersUrlIsCalled() {
        // given
        List<Order> savedOrders = List.of(
                Order.builder().id(UUID.randomUUID()).productId(UUID.randomUUID()).userId(UUID.fromString(USER_ID)).isNew(true).build(),
                Order.builder().id(UUID.randomUUID()).productId(UUID.randomUUID()).userId(UUID.fromString(USER_ID)).isNew(true).build(),
                Order.builder().id(UUID.randomUUID()).productId(UUID.randomUUID()).userId(UUID.fromString(USER_ID)).isNew(true).build()
        );
        orderRepository.saveAll(savedOrders).blockLast();

        // when - then
        webTestClient
                .mutateWith(JWT)
                .get().uri("/order")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Order.class)
                .hasSize(3)
                .consumeWith(response -> {
                    List<Order> orders = response.getResponseBody();
                    assertThat(orders).allMatch(order -> USER_ID.equals(order.getUserId().toString()));
                });
    }

    @Test
    void shouldReturnErrorMessage_whenInvalidSubjectInJwt() {
        // given
        SecurityMockServerConfigurers.JwtMutator invalidJwt = mockJwt().jwt(jwt -> jwt
                .claim("sub", "invalid").claim("email", "invalid@test.com"));

        // when - then
        webTestClient
                .mutateWith(invalidJwt)
                .get().uri("/order")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .consumeWith(response -> {
                    ErrorResponse errorResponse = response.getResponseBody();
                    assertThat(errorResponse).isNotNull();
                    assertThat(errorResponse.getReason()).contains("Invalid UUID string");
                });
    }

    @Test
    @WithMockUser(value = USER_ID)
    void shouldSaveOrderAndSendEmail_whenSaveOrderUrlIsCalledAndValidProduct() {
        // given
        UUID randomUUID = UUID.randomUUID();
        stubForProductWithId(randomUUID);

        // when - then
        webTestClient
                .mutateWith(JWT)
                .post().uri("/order/" + randomUUID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Order.class)
                .consumeWith(response -> {
                    // then
                    Order order = response.getResponseBody();
                    assertThat(order).isNotNull();
                    assertThat(order.getUserId().toString()).isEqualTo(USER_ID);
                    assertThat(order.getProductId().toString()).isEqualTo(randomUUID.toString());

                    Order dbOrder = orderRepository.findAllByUserIdOrderByCreatedDateDesc(UUID.fromString(USER_ID)).blockFirst();
                    assertThat(dbOrder).isNotNull();
                    assertThat(dbOrder.getProductId()).isEqualTo(randomUUID);

                    UserOrder userOrder = poll(output, "sendEmail", UserOrder.class);
                    assertThat(userOrder).isNotNull();
                    assertThat(userOrder.getEmail()).isEqualTo(EMAIL);
                    assertThat(userOrder.getPrice()).isEqualTo(100.0);
                    assertThat(userOrder.getProduct()).isEqualTo("Product 1");
                });
    }

    @Test
    void shouldReturnErrorMessage_whenPlaceOrderWithInvalidSubjectInJwt() {
        // given
        SecurityMockServerConfigurers.JwtMutator invalidJwt = mockJwt().jwt(jwt -> jwt
                .claim("sub", "invalid"));
        // when - then
        webTestClient
                .mutateWith(invalidJwt)
                .post().uri("/order/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .consumeWith(response -> {
                    ErrorResponse errorResponse = response.getResponseBody();
                    assertThat(errorResponse).isNotNull();
                    assertThat(errorResponse.getReason()).contains("Invalid UUID string");
                });
    }

    @Test
    void shouldReturnErrorMessage_whenPlaceOrderWithMissingEmailInJwt() {
        // given
        UUID randomUUID = UUID.randomUUID();
        stubForProductWithId(randomUUID);

        SecurityMockServerConfigurers.JwtMutator invalidJwt = mockJwt().jwt(jwt -> jwt
                .claim("sub", USER_ID));

        // when - then
        webTestClient
                .mutateWith(invalidJwt)
                .post().uri("/order/" + randomUUID)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .consumeWith(response -> {
                    ErrorResponse errorResponse = response.getResponseBody();
                    assertThat(errorResponse).isNotNull();
                    assertThat(errorResponse.getReason()).contains("No email found in the request");
                });
    }

    @ParameterizedTest
    @WithMockUser(value = USER_ID)
    @ValueSource(ints = {400, 401, 404})
    void shouldReturnErrorMessage_whenPlaceOrderAndErrorInProductEndpoint(int statusCode) {
        // given
        stubForError(statusCode);

        // when - then
        webTestClient
                .mutateWith(JWT)
                .post().uri("/order/" + UUID.randomUUID())
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ErrorResponse.class)
                .consumeWith(response -> {
                    ErrorResponse errorResponse = response.getResponseBody();
                    assertThat(errorResponse).isNotNull();
                    assertThat(errorResponse.getReason()).contains(String.valueOf(statusCode));
                });
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

    private static void stubForError(int httpStatus) {
        wireMockServer.stubFor(WireMock.get(urlPathTemplate("/product/{id}"))
                .willReturn(aResponse()
                        .withStatus(httpStatus)));
    }
}
