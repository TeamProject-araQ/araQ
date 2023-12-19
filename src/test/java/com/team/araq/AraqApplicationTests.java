package com.team.araq;

import com.team.araq.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class AraqApplicationTests {
    @Autowired
    UserRepository userRepository;

    @Test
    void contextLoads() throws IOException {

    }
}
