package com.team.araq.plaza;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageHandler {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;

    @MessageMapping("/plaza/join")
    @SendTo("/topic/plaza/join")
    public Avatar join(@Payload String username) {
        SiteUser user = userService.getByUsername(username);
        userService.setStatusInPlaza(user, true);
        return new Avatar(user.getUsername(), user.getNickName(), user.getImage());
    }

    @MessageMapping("/plaza/exit")
    @SendTo("/topic/plaza/exit")
    public String exit(@Payload String username) {
        SiteUser user = userService.getByUsername(username);
        userService.setStatusInPlaza(user, false);
        return username;
    }

    @MessageMapping("/plaza/message")
    @SendTo("/topic/plaza/message")
    public String handleMessage(@Payload String message) {
        return message;
    }

    @MessageMapping("/plaza/location")
    @SendTo("/topic/plaza/location")
    public String handleLocation(@Payload String message) {
        JSONObject data = new JSONObject(message);
        SiteUser user = userService.getByUsername(data.get("sender").toString());
        userService.setUserLocationInPlaza(user, data.get("top").toString(), data.get("left").toString());
        return message;
    }

    @MessageMapping("/plaza/focus")
    @SendTo("/topic/plaza/focus")
    public String handleStatus(@Payload String message) {
        JSONObject data = new JSONObject(message);
        SiteUser user = userService.getByUsername(data.get("sender").toString());
        userService.setFocusInPlaza(user, data.get("status").toString());
        return message;
    }
}
