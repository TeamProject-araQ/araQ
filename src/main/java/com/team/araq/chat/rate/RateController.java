package com.team.araq.chat.rate;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/rate")
public class RateController {

    private final RateService rateService;

    private final UserService userService;

    @PostMapping("/save")
    @ResponseBody
    public String save(@RequestBody String data, Principal principal) {
        JSONObject jsonObject = new JSONObject(data);
        SiteUser user1 = this.userService.getByUsername(principal.getName());
        SiteUser user2 = this.userService.getByUsername(jsonObject.getString("target"));
        this.rateService.saveRate(user1, user2, jsonObject.getDouble("manner"), jsonObject.getDouble("appeal"), jsonObject.getDouble("appearance"));
        return "평가가 저장되었습니다.";
    }
}
