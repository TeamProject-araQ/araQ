package com.team.araq.pay;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final UserService userService;

    private final PaymentService paymentService;

    @GetMapping("/payment")
    public String pay() {
        return "user/payment";
    }

    @PostMapping("/charge")
    @ResponseBody
    public String charge(@RequestBody PaymentDTO paymentDTO) {
        this.paymentService.savePayment(paymentDTO);
        this.userService.plusBubbles(this.userService.getByUsername(paymentDTO.getUsername()), paymentDTO.getBubble());
        return paymentDTO.getBubble() + " 버블이 충전되었습니다.";
    }
}
