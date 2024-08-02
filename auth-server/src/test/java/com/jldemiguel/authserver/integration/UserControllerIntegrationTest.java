package com.jldemiguel.authserver.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jldemiguel.authserver.model.ErrorResponse;
import com.jldemiguel.authserver.model.User;
import com.jldemiguel.authserver.model.UserDto;
import com.jldemiguel.authserver.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUser_whenUsernameAndEmailAreUnique() throws Exception {
        //given
        UserDto userDto = UserDto.builder()
                .username("test")
                .email("test@test.com")
                .password("123456")
                .build();

        //when
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());

        //then
        User userByUsername = userRepository.findByUsernameOrEmail("test");
        assertEquals("test", userByUsername.getUsername());
        assertEquals("test@test.com", userByUsername.getEmail());

        User userByEmail = userRepository.findByUsernameOrEmail("test@test.com");
        assertEquals("test", userByEmail.getUsername());
        assertEquals("test@test.com", userByEmail.getEmail());
    }

    @Test
    void shouldReturnError_whenUsernameAndEmailAreNotUnique() throws Exception {
        //given
        userRepository.save(User.builder().username("test").email("test@test.com").build());
        UserDto userDto = UserDto.builder()
                .username("test")
                .email("test@test.com")
                .password("123456")
                .build();

        //when
        MvcResult result = mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andReturn();

        //then
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertEquals("Username or email already exists", response.getReason());
    }
}
