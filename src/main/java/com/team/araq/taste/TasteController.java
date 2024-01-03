package com.team.araq.taste;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
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
    @GetMapping("/modify")
    public String modify(TasteDTO tasteDTO, Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        Taste taste = this.tasteService.getTaste(user);
        tasteDTO.setOption1(taste.getOption1());
        tasteDTO.setOption2(taste.getOption2());
        tasteDTO.setOption3(taste.getOption3());
        tasteDTO.setOption4(taste.getOption4());
        tasteDTO.setOption5(taste.getOption5());
        tasteDTO.setOption6(taste.getOption6());
        tasteDTO.setOption7(taste.getOption7());
        tasteDTO.setOption8(taste.getOption8());
        tasteDTO.setOption9(taste.getOption9());
        tasteDTO.setOption10(taste.getOption10());
        return "taste/taste";
    }

    @PostMapping("/modify")
    public String modify(@Valid TasteDTO tasteDTO, BindingResult bindingResult, Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        Taste taste = this.tasteService.getTaste(user);
        this.tasteService.modifyTaste(taste, tasteDTO);
        return "redirect:/";
    }

}
