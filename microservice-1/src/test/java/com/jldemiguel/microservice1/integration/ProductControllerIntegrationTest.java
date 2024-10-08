package com.jldemiguel.microservice1.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jldemiguel.microservice1.model.jpa.Product;
import com.jldemiguel.microservice1.model.response.ErrorResponse;
import com.jldemiguel.microservice1.repository.ProductsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductsRepository productsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productsRepository.deleteAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/product", "/product/id"})
    void shouldReturn401_whenUnauthorizedRequest(String path) throws Exception {
        mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser")
    void shouldGetAllProducts_whenGetAllProductsUrlIsCalled() throws Exception {
        // given
        productsRepository.save(Product.builder().id(UUID.randomUUID()).name("Product 1").price(100.0).build());
        productsRepository.save(Product.builder().id(UUID.randomUUID()).name("Product 2").price(200.0).build());
        productsRepository.save(Product.builder().id(UUID.randomUUID()).name("Product 3").price(300.0).build());

        // when
        MvcResult result = mockMvc.perform(get("/product")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        // then
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        Product[] products = objectMapper.readValue(jsonNode.get("content").toString(), Product[].class);

        assertTrue(Arrays.stream(products).anyMatch(p -> p.getName().equals("Product 1") && p.getPrice() == 100.0));
        assertTrue(Arrays.stream(products).anyMatch(p -> p.getName().equals("Product 2") && p.getPrice() == 200.0));
        assertTrue(Arrays.stream(products).anyMatch(p -> p.getName().equals("Product 3") && p.getPrice() == 300.0));
    }

    @ParameterizedTest
    @MethodSource("paginationParameters")
    @WithMockUser(username = "testUser")
    void shouldGetPaginatedProducts_whenGetAllProductsUrlIsCalled(int page, int size, int expected) throws Exception {
        // given
        productsRepository.save(Product.builder().id(UUID.randomUUID()).name("Product 1").price(100.0).build());
        productsRepository.save(Product.builder().id(UUID.randomUUID()).name("Product 2").price(200.0).build());
        productsRepository.save(Product.builder().id(UUID.randomUUID()).name("Product 3").price(300.0).build());

        // when
        MvcResult result = mockMvc.perform(get("/product?page=" + page + "&size=" + size)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        // then
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        Product[] products = objectMapper.readValue(jsonNode.get("content").toString(), Product[].class);

        assertEquals(expected, products.length);
    }

    private static Stream<Arguments> paginationParameters() {
        return Stream.of(
                Arguments.of(0, 1, 1),  // Page 0, Size 1, Expect 1 product
                Arguments.of(0, 2, 2),  // Page 0, Size 2, Expect 2 products
                Arguments.of(1, 2, 1),  // Page 1, Size 2, Expect 1 product
                Arguments.of(0, 3, 3)   // Page 0, Size 3, Expect 3 products
        );
    }

    @Test
    @WithMockUser(username = "testUser")
    void shouldGetProductById_whenGetProductByIdUrlIsCalled() throws Exception {
        // given
        Product savedProduct = productsRepository.save(Product.builder().id(UUID.randomUUID()).name("Product 1").price(100.0).build());

        // when
        MvcResult result = mockMvc.perform(get("/product/" + savedProduct.getId())
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        // then
        Product product = objectMapper.readValue(result.getResponse().getContentAsString(), Product.class);

        assertEquals(savedProduct.getId(), product.getId());
        assertEquals("Product 1", product.getName());
        assertEquals(100.0, product.getPrice());
    }

    @Test
    @WithMockUser(username = "testUser")
    void shouldReturn400_whenGetProductByIdNotFound() throws Exception {
        // when
        UUID randomUUID = UUID.randomUUID();
        MvcResult result = mockMvc.perform(get("/product/" + randomUUID)
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);

        assertEquals("Product with ID: " + randomUUID + " not found", response.getReason());
    }

    @Test
    public void shouldReturnCreated_WhenCreateProduct_withAdminRole() throws Exception {
        Product product = Product.builder().name("Test Product").price(100.0).build();

        mockMvc.perform(post("/product")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated());
    }

    @Test
    public void shouldNotReturnCreated_WhenCreateProduct_withUserRole() throws Exception {
        Product product = Product.builder().name("Test Product").price(100.0).build();

        mockMvc.perform(post("/product")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isForbidden());
    }
}
