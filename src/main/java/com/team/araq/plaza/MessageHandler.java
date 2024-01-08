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
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageHandler {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;
    private final PlazaService plazaService;

    @MessageMapping("/plaza/join/{code}")
    @SendTo("/topic/plaza/join/{code}")
    public Avatar join(@Payload String username, @DestinationVariable String code) {
        SiteUser user = userService.getByUsername(username);
        userService.setLocation(user, code);
        plazaService.setPeople(plazaService.getByCode(code), userService.getByLocation(code).size());
        return new Avatar(user.getUsername(), user.getNickName(), user.getImage());
    }

    @MessageMapping("/plaza/exit/{code}")
    @SendTo("/topic/plaza/exit/{code}")
    public String exit(@Payload String username, @DestinationVariable String code) {
        Plaza plaza = plazaService.getByCode(code);
        SiteUser user = userService.getByUsername(username);
        userService.setLocation(user, "");
        plazaService.setPeople(plaza, userService.getByLocation(code).size());
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
}
