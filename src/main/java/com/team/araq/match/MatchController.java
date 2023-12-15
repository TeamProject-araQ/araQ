package com.team.araq.match;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchController {
    private final UserService userService;

    @GetMapping("/around")
    public String around(Model model, Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        return "match";
    }

    @GetMapping("/personality")
    public String personality() {
        return "match";
    }
}
