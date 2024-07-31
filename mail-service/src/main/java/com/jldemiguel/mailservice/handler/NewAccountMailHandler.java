package com.jldemiguel.mailservice.handler;

import com.jldemiguel.mailservice.model.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewAccountMailHandler {

    public void sendNewAccountEmail(UserDto user) {
        //TODO
    }
}
