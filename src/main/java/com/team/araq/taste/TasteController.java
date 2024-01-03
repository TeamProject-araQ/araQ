package com.team.araq.taste;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/taste")
public class TasteController {

    private final TasteService tasteService;

    private final UserService userService;

    @GetMapping("/main")
    public String main() {
        return "taste/main";
    }

    @GetMapping("/check")
    public String check(TasteDTO tasteDTO) {
        return "taste/taste";
    }

    @PostMapping("/check")
    public String check(@Valid TasteDTO tasteDTO, Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        this.tasteService.saveTaste(user, tasteDTO);
        return "redirect:/";
    }
}
