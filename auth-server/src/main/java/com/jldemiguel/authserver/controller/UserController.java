package com.jldemiguel.authserver.controller;

import com.jldemiguel.authserver.model.UserDto;
import com.jldemiguel.authserver.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody UserDto user) {
        log.info("Registering user with username : {}, and email: {}", user.getUsername(), user.getEmail());
        userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
