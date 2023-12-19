package com.team.araq.chat;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;
    private final RoomService roomService;

    @MessageMapping("/send")
    public void sendMessage(ChatDto chatDto) {
        simpMessagingTemplate.convertAndSend("/topic/chat/" + chatDto.getCode(), chatDto);
    }

    @MessageMapping("/alert")
    public void alert(MessageDto messageDto, Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        messageDto.setNickname(user.getNickName());
        messageDto.setContent(user.getNickName() + "님이 거절하셨습니다.");
        simpMessagingTemplate.convertAndSend("/topic/all/" + messageDto.getTarget(), messageDto);
    }

    @PostMapping("/create")
    @ResponseBody
    public String chat(Principal principal, @RequestBody String targetName) {
        SiteUser user = userService.getByUsername(principal.getName());
        SiteUser target = userService.getByUsername(targetName);
        String uuid = UUID.randomUUID().toString();

        roomService.create(uuid, user, target);

        MessageDto messageDto = new MessageDto("acceptChat", user.getNickName(), null, null,
                null, uuid, null);
        simpMessagingTemplate.convertAndSend("/topic/all/" + targetName, messageDto);
        return uuid;
    }

    @GetMapping("/join/{code}")
    public String join(Model model, Principal principal, @PathVariable("code") String code) {
        SiteUser user = userService.getByUsername(principal.getName());
        Room room = roomService.get(code);

        if (!roomService.check(room, user)) throw new RuntimeException("권한이 없습니다.");

        model.addAttribute("user", user);
        model.addAttribute("room", room);
        return "conn/chat";
    }

    @GetMapping("/list")
    public String room(Model model, Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        List<Room> roomList = roomService.getList(user);

        model.addAttribute("roomList", roomList);
        return "conn/room";
    }

    @PostMapping("/request")
    @ResponseBody
    public String request(Model model, Principal principal, @RequestBody String username) {
        SiteUser user = userService.getByUsername(principal.getName());
        MessageDto messageDto = new MessageDto("chatRequest", user.getUsername(), user.getAge(),
                user.getIntroduce(), user.getImage(), username + "님이 채팅을 신청했습니다.", username);
        simpMessagingTemplate.convertAndSend("/topic/all/" + username, messageDto);
        return null;
    }
}
