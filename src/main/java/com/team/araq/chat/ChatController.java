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
        simpMessagingTemplate.convertAndSend("/topic/chat", chatDto);
    }

    @GetMapping("/create/{username}")
    public String chat(Model model, Principal principal, @PathVariable(value = "username") String username) {
        SiteUser user = userService.getByUsername(principal.getName());
        SiteUser target = userService.getByUsername(username);
        String uuid = UUID.randomUUID().toString();

        roomService.create(uuid, user, target);

        return "redirect:/chat/list";
    }

    @PostMapping("/join")
    public String join(Model model, Principal principal, @RequestParam(value = "code") String code) {
        SiteUser user = userService.getByUsername(principal.getName());
        Room room = roomService.get(code);

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
}
