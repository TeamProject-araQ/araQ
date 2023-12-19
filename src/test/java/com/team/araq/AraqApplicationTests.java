package com.team.araq;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class AraqApplicationTests {
	@Autowired
	UserService userService;

    @Test
	void contextLoads() throws IOException {
		// 관리자 계정 생성
		// this.userService.createAdmin();
	}
}
