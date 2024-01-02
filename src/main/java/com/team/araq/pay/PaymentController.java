package com.team.araq.pay;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final UserService userService;

    private final PaymentService paymentService;

    private final HistoryService historyService;

    @GetMapping("/payment")
    public String charge() {
        return "payment";
    }

    @PostMapping("/charge")
    @ResponseBody
    public String charge(@RequestBody PaymentDTO paymentDTO) {
        this.paymentService.savePayment(paymentDTO);
        this.userService.plusBubbles(this.userService.getByUsername(paymentDTO.getUsername()), paymentDTO.getBubble());
        return paymentDTO.getBubble() + " 버블이 충전되었습니다.";
    }

    @PostMapping("/pay")
    @ResponseBody
    public boolean pay(Principal principal, @RequestBody String username) {
        SiteUser user1 = this.userService.getByUsername(principal.getName());
        SiteUser user2 = this.userService.getByUsername(username);
        if (user1.getBubble() > 500) {
            this.userService.useBubble(user1, 500);
            this.userService.openVoice(user1, user2);
            this.historyService.saveHistory(user1, 500, user1.getBubble(), "음성 듣기");
            return true;
        } else
            return false;
    }

    @PostMapping("/pay/cancel")
    @ResponseBody
    public String cancelPayment(@RequestBody String impUid) throws Exception {
        Payment payment = this.paymentService.getPayment(impUid);
        if (payment.getStatus().equals("cancelled"))
            return "이미 취소된 결제입니다.";
        else if (payment.getUser().getBubble() < payment.getAmount())
            return "이미 사용된 결제 내역입니다.";
        else {
            this.paymentService.cancelPayment(impUid);
            this.paymentService.updatePayment(payment, impUid);
            this.userService.minusBubbles(payment.getUser(), payment.getAmount());
        }

        return "결제가 취소되었습니다.";
    }
}
