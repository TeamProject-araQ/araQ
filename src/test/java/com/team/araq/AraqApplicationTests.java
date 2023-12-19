package com.team.araq;

import com.team.araq.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest
class AraqApplicationTests {
    @Autowired
    UserRepository userRepository;

    @Test
    void contextLoads() throws IOException {
        // 관리자 계정 생성
        // this.userService.createAdmin();

        FileInputStream file = new FileInputStream(new File("dummy.xlsx"));
    }
}
