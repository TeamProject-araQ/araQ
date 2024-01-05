package com.team.araq.plaza;

import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/plaza")
public class PlazaController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;

    @GetMapping("/join")
    public String join(Model model, Principal principal) {
        model.addAttribute("onlinePlazaUsers", userService.getOnlineInPlaza());
        return "plaza/plaza";
    }
}
