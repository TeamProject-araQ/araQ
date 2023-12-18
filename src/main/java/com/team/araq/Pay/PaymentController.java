package com.team.araq.Pay;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final UserService userService;

    @GetMapping("/payment")
    public String pay() {
        return "user/payment";
    }

    @PostMapping("/charge")
    @ResponseBody
    public String charge(@RequestParam("bubble") int bubble, Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        this.userService.addBubbles(user, bubble);
        return bubble + "버블이 충전되었습니다.";
    }

}
