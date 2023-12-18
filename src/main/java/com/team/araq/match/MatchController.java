package com.team.araq.match;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchController {
    private final UserService userService;

    @GetMapping("/around")
    public String around(Model model, Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        String address = user.getAddress();
        String[] words = address.split(" ");
        String gender = user.getGender().equals("남성") ? "여성" : "남성";
        List<SiteUser> userList = userService.getByAddress(words[0] + " " + words[1], gender);
        model.addAttribute("userList", userList);
        return "conn/match";
    }

    @GetMapping("/personality")
    public String personality() {
        return "conn/match";
    }
}
