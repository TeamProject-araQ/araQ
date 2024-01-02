package com.team.araq.taste;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/taste")
public class TasteController {

    private final TasteService tasteService;

    @GetMapping("/main")
    public String main() {
        return "taste/main";
    }

    @GetMapping("/check")
    public String check() {
        return "taste/taste";
    }
}
