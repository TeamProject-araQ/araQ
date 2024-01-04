package com.team.araq.plaza;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageHandler {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;

    @MessageMapping("/plaza/join")
    public void join(@Payload String username) {
        SiteUser user = userService.getByUsername(username);
        Avatar avatar = new Avatar(user.getUsername(), user.getNickName(), user.getImage());
        simpMessagingTemplate.convertAndSend("/topic/plaza/join", avatar);
    }
}
