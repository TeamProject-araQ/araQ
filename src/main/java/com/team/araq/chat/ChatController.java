package com.team.araq.chat;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;

    @GetMapping("/with/{username}")
    public String chat(Model model, Principal principal, @PathVariable(value = "username") String username) {
        SiteUser user = userService.getByUsername(principal.getName());
        SiteUser target = userService.getByUsername(username);
        String uuid = UUID.randomUUID().toString();
        return "conn/chat";
    }

    @MessageMapping("/send")
    public void sendMessage(ChatDto chatDto) {
        simpMessagingTemplate.convertAndSend("/topic/chat", chatDto);
    }

    @GetMapping("/room")
    public String room() {
       return "conn/room";
    }
}
