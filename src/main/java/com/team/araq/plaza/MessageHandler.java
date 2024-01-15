package com.team.araq.plaza;

import com.team.araq.chat.MessageDto;
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
    public void join(@Payload String username, @DestinationVariable String code) {
        SiteUser user = userService.getByUsername(username);
        plazaService.setPeople(plazaService.getByCode(code), userService.getByLocation(code).size());
        simpMessagingTemplate.convertAndSend("/topic/plaza/join/" + code,
                new Avatar(user.getUsername(), user.getNickName(), user.getImage(), user.getChatBackground(), user.getChatColor()));
        simpMessagingTemplate.convertAndSend("/topic/plaza/notice/" + code,
                user.getNickName() + "님이 입장하셨습니다.");
    }

    @MessageMapping("/plaza/exit/{code}")
    public void exit(@Payload String username, @DestinationVariable String code) {
        Plaza plaza = plazaService.getByCode(code);
        SiteUser user = userService.getByUsername(username);
        userService.setLocation(user, "");
        plazaService.setPeople(plaza, userService.getByLocation(code).size());
        simpMessagingTemplate.convertAndSend("/topic/plaza/exit/" + code, username);
        simpMessagingTemplate.convertAndSend("/topic/plaza/notice/" + code,
                user.getNickName() + "님이 퇴장하셨습니다.");
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

    @MessageMapping("/plaza/fire/{code}")
    public void fire(@Payload String message, @DestinationVariable String code) {
        SiteUser user = userService.getByUsername(message);
        simpMessagingTemplate.convertAndSend("/topic/plaza/fire/" + code + "/" + message, "fire");
        simpMessagingTemplate.convertAndSend("/topic/plaza/notice/" + code,
                user.getNickName() + "님이 강제퇴장 당하였습니다.");
        MessageDto messageDto = new MessageDto();
        messageDto.setType("refuse");
        messageDto.setContent("강제퇴장 당하였습니다.");
        simpMessagingTemplate.convertAndSend("/topic/all/" + message, messageDto);
    }
}
