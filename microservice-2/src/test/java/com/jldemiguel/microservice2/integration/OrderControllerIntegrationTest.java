package com.jldemiguel.microservice2.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.jldemiguel.microservice2.config.TestConfig;
import com.jldemiguel.microservice2.model.Product;
import com.jldemiguel.microservice2.model.UserOrder;
import com.jldemiguel.microservice2.model.jpa.Order;
import com.jldemiguel.microservice2.model.reponse.ErrorResponse;
import com.jldemiguel.microservice2.model.reponse.OrderDto;
import com.jldemiguel.microservice2.repository.OrderRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.jldemiguel.microservice2.config.CacheConfig.CACHE_NAME;
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

    @Autowired
    private CacheManager cacheManager;

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void setUpAll() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(PORT));
        wireMockServer.start();
    }

    @BeforeEach
    void setUp() {
        cacheManager.getCache(CACHE_NAME).clear();
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
        buildSaveAndStubOrder(3);

        // when - then
        webTestClient
                .mutateWith(JWT)
                .get().uri("/order")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderDto.class)
                .hasSize(3)
                .consumeWith(response -> {
                    List<OrderDto> orders = response.getResponseBody();
                    assertThat(orders).hasSize(3);
                    assertThat(orders).allMatch(order -> order.getName().contains("Product")
                            && order.getPrice() == 100.0
                            && order.getCreatedDate() != null);
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
    void shouldPopulateCache_whenOrderUrlIsCalledAndProductIsNotInCache() {
        // given
        Order savedOrder = buildSaveAndStubOrder(1).get(0);

        // when
        webTestClient
                .mutateWith(JWT)
                .get().uri("/order")
                .exchange()
                .expectStatus().isOk();
        //then
        Product product = cacheManager.getCache(CACHE_NAME).get(savedOrder.getProductId(), Product.class);
        assertThat(product).isNotNull();
        assertThat(product.getName()).isEqualTo("Product 1");
        assertThat(product.getPrice()).isEqualTo(100.0);
    }

    @Test
    @WithMockUser(value = USER_ID)
    void shouldHitCache_whenOrderUrlIsCalledAndProductIsInCache() {
        // given
        Order savedOrder = buildAndSave(1).get(0);
        cacheManager.getCache(CACHE_NAME).put(savedOrder.getProductId(),
                new Product(savedOrder.getProductId(), "Product 1", 100.0));

        // when
        webTestClient
                .mutateWith(JWT)
                .get().uri("/order")
                .exchange()
                .expectStatus().isOk();
        //then
        Product product = cacheManager.getCache(CACHE_NAME).get(savedOrder.getProductId(), Product.class);
        assertThat(product).isNotNull();
        assertThat(product.getName()).isEqualTo("Product 1");
        assertThat(product.getPrice()).isEqualTo(100.0);

        wireMockServer.verify(0, getRequestedFor(urlEqualTo("/product/" + savedOrder.getProductId())));
    }

    @Test
    @WithMockUser(value = USER_ID)
    void shouldSaveOrderAndSendEmail_whenSaveOrderUrlIsCalledAndValidProduct() {
        // given
        UUID randomUUID = UUID.randomUUID();
        stubForProduct(randomUUID, "Product 1");

        // when - then
        webTestClient
                .mutateWith(JWT)
                .post().uri("/order/" + randomUUID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderDto.class)
                .consumeWith(response -> {
                    // then
                    OrderDto order = response.getResponseBody();
                    assertThat(order).isNotNull();
                    assertThat(order.getName()).isEqualTo("Product 1");
                    assertThat(order.getPrice()).isEqualTo(100.0);
                    assertThat(order.getCreatedDate()).isNotNull();

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
        stubForProduct(randomUUID, "Product 1");

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

    @Test
    @WithMockUser(value = USER_ID)
    void shouldPopulateCache_whenPlaceOrderUrlIsCalledAndProductIsNotInCache() {
        // given
        UUID randomUUID = UUID.randomUUID();
        stubForProduct(randomUUID, "Product 1");

        // when - then
        webTestClient
                .mutateWith(JWT)
                .post().uri("/order/" + randomUUID)
                .exchange()
                .expectStatus().isOk();

        Product product = cacheManager.getCache(CACHE_NAME).get(randomUUID, Product.class);
        assertThat(product).isNotNull();
        assertThat(product.getName()).isEqualTo("Product 1");
        assertThat(product.getPrice()).isEqualTo(100.0);
    }

    @Test
    @WithMockUser(value = USER_ID)
    void shouldHitCache_whenPlaceOrderUrlIsCalledAndProductIsNotInCache() {
        // given
        UUID randomUUID = UUID.randomUUID();
        cacheManager.getCache(CACHE_NAME).put(randomUUID,
                new Product(randomUUID, "Product 1", 100.0));

        // when - then
        webTestClient
                .mutateWith(JWT)
                .post().uri("/order/" + randomUUID)
                .exchange()
                .expectStatus().isOk();

        Product product = cacheManager.getCache(CACHE_NAME).get(randomUUID, Product.class);
        assertThat(product).isNotNull();
        assertThat(product.getName()).isEqualTo("Product 1");
        assertThat(product.getPrice()).isEqualTo(100.0);
        wireMockServer.verify(0, getRequestedFor(urlEqualTo("/product/" + randomUUID)));
    }

    private static void stubForProduct(UUID id, String name) {
        wireMockServer.stubFor(WireMock.get("/product/" + id)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "id": "%s",
                                    "name": "%s",
                                    "price": 100.0
                                }
                                """.formatted(id, name))));
    }

    private static void stubForError(int httpStatus) {
        wireMockServer.stubFor(WireMock.get(urlPathTemplate("/product/{id}"))
                .willReturn(aResponse()
                        .withStatus(httpStatus)));
    }

    private static Order buildNewOrder() {
        return Order.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .userId(UUID.fromString(USER_ID))
                .isNew(true)
                .build();
    }

    public List<Order> buildAndSave(int count) {
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Order order = buildNewOrder();
            orders.add(order);
        }
        orderRepository.saveAll(orders).blockLast();
        return orders;
    }

    public List<Order> buildSaveAndStubOrder(int count) {
        List<Order> orders = buildAndSave(count);
        for (int i = 0; i < orders.size(); i++) {
            stubForProduct(orders.get(i).getProductId(), "Product " + (i + 1));
        }
        return orders;
    }
}
