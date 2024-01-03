package com.team.araq.idealType;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/idealType")
public class IdealTypeController {

    @GetMapping("/choose")
    public String idealType() {
        return "user/idealType";
    }
}
