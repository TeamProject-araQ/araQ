package com.team.araq.match;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/match")
public class MatchController {

    @GetMapping("/around")
    public String around() {
        return "match";
    }
}
