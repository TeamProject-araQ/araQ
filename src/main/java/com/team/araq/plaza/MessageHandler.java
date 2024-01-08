package com.team.araq.plaza;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MessageHandler {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;

    @MessageMapping("/plaza/join/{code}")
    @SendTo("/topic/plaza/join/{code}")
    public Avatar join(@Payload String username, @DestinationVariable String code) {
        SiteUser user = userService.getByUsername(username);
        userService.setStatusInPlaza(user, true);
        return new Avatar(user.getUsername(), user.getNickName(), user.getImage());
    }

    @MessageMapping("/plaza/exit/{code}")
    @SendTo("/topic/plaza/exit/{code}")
    public String exit(@Payload String username, @DestinationVariable String code) {
        SiteUser user = userService.getByUsername(username);
        userService.setStatusInPlaza(user, false);
        return username;
    }

    @MessageMapping("/plaza/message/{code}")
    @SendTo("/topic/plaza/message/{code}")
    public String handleMessage(@Payload String message, @DestinationVariable String code) {
        return message;
    }

    @MessageMapping("/plaza/location/{code}")
    @SendTo("/topic/plaza/location/{code}")
    public String handleLocation(@Payload String message, @DestinationVariable String code) {
        JSONObject data = new JSONObject(message);
        SiteUser user = userService.getByUsername(data.get("sender").toString());
        userService.setUserLocationInPlaza(user, data.get("top").toString(), data.get("left").toString());
        return message;
    }

    @MessageMapping("/plaza/focus/{code}")
    @SendTo("/topic/plaza/focus/{code}")
    public String handleStatus(@Payload String message, @DestinationVariable String code) {
        JSONObject data = new JSONObject(message);
        SiteUser user = userService.getByUsername(data.get("sender").toString());
        userService.setFocusInPlaza(user, data.get("status").toString());
        return message;
    }

    @Scheduled(fixedRate = 60000)
    public void ping() {
        List<SiteUser> users = userService.getOnlineInPlaza();
        for (SiteUser user : users) userService.setStatusInPlaza(user, false);
        simpMessagingTemplate.convertAndSend("/topic/plaza/ping", "ping");
    }

    @MessageMapping("/plaza/pong")
    public void pong(String username) {
        SiteUser user = userService.getByUsername(username);
        userService.setStatusInPlaza(user, true);
    }
}
