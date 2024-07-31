package com.jldemiguel.authserver.service;

import com.jldemiguel.authserver.model.User;
import com.jldemiguel.authserver.model.UserDto;
import com.jldemiguel.authserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    public static final String USER_ROLE = "USER";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public void registerUser(UserDto user) {
        User newUser = User.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .password(passwordEncoder.encode(user.getPassword()))
                .role(USER_ROLE)
                .build();
        userRepository.save(newUser);

        mailService.sendEmail(user);
    }
}
