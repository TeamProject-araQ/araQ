package com.team.araq.taste;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/test/{username}")
    public String test(@PathVariable("username") String username, Model model) {
        SiteUser target = this.userService.getByUsername(username);
        model.addAttribute("target", target);
        return "taste/test";
    }

    @PostMapping("/test")
    @ResponseBody
    public int test(@RequestBody Map<String, String> options) {
        SiteUser user = this.userService.getByUsername(options.get("username"));
        Taste taste = this.tasteService.getTaste(user);
        int score = 0;
        if (taste.getOption1().equals(options.get("option1"))) score += 10;
        if (taste.getOption2().equals(options.get("option2"))) score += 10;
        if (taste.getOption3().equals(options.get("option3"))) score += 10;
        if (taste.getOption4().equals(options.get("option4"))) score += 10;
        if (taste.getOption5().equals(options.get("option5"))) score += 10;
        if (taste.getOption6().equals(options.get("option6"))) score += 10;
        if (taste.getOption7().equals(options.get("option7"))) score += 10;
        if (taste.getOption8().equals(options.get("option8"))) score += 10;
        if (taste.getOption9().equals(options.get("option9"))) score += 10;
        if (taste.getOption10().equals(options.get("option10"))) score += 10;
        return score;
    }

    @PostMapping("/result")
    public String result(String username, String options, Model model) {
        SiteUser user = this.userService.getByUsername(username);
        JSONObject jsonObject = new JSONObject(options);
        List<String> answers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String value = "option" + i;
            answers.add((String) jsonObject.get(value));
        }
        model.addAttribute("target", user);
        model.addAttribute("answers", answers);
        model.addAttribute("score", jsonObject.get("score"));
        return "taste/result";
    }
}
