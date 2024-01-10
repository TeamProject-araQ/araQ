package com.team.araq.pay;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
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
        return "payment/payment";
    }

    @PostMapping("/charge")
    @ResponseBody
    public String charge(@RequestBody PaymentDTO paymentDTO) {
        this.paymentService.savePayment(paymentDTO);
        this.userService.plusBubbles(this.userService.getByUsername(paymentDTO.getUsername()), paymentDTO.getBubble());
        return paymentDTO.getBubble() + " 버블이 충전되었습니다.";
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

    @PostMapping("/purchase")
    @ResponseBody
    public String purchase(@RequestBody String purchase, Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        JSONObject jsonObject = new JSONObject(purchase);
        String ticketName = jsonObject.getString("ticketName");
        int bubble = jsonObject.getInt("bubble");
        if (bubble > user.getBubble()) return "보유하신 버블이 부족합니다.";
        else if (ticketName.contains("매칭 우선권")) {
            if (user.isPreference()) return "이미 매칭 우선권을 보유중 입니다.";
            else {
                switch (ticketName) {
                    case "매칭 우선권 (1일)" -> this.userService.getPreference(user, 1);
                    case "매칭 우선권 (7일)" -> this.userService.getPreference(user, 7);
                    case "매칭 우선권 (30일)" -> this.userService.getPreference(user, 30);
                }
            }
        } else if (ticketName.contains("음성 이용권")) {
            if (user.isListenVoice()) return "이미 음성 이용권을 보유중 입니다.";
            else {
                switch (ticketName) {
                    case "음성 이용권 (1일)" -> this.userService.getListenVoice(user, 1);
                    case "음성 이용권 (7일)" -> this.userService.getListenVoice(user, 7);
                    case "음성 이용권 (30일)" -> this.userService.getListenVoice(user, 30);
                }
            }
        } else if (ticketName.contains("아라큐 신청권")) {
            switch (ticketName) {
                case "아라큐 신청권 (1개)" -> this.userService.addAraQPass(user, 1);
                case "아라큐 신청권 (5개)" -> this.userService.addAraQPass(user, 5);
                case "아라큐 신청권 (10개)" -> this.userService.addAraQPass(user, 10);
            }
        } else if (ticketName.contains("채팅 신청권")) {
            switch (ticketName) {
                case "채팅 신청권 (1개)" -> this.userService.addChatPass(user, 1);
                case "채팅 신청권 (3개)" -> this.userService.addChatPass(user, 3);
                case "채팅 신청권 (5개)" -> this.userService.addChatPass(user, 5);
            }
        }
        this.userService.useBubble(user, bubble);
        this.historyService.saveHistory(user, bubble, user.getBubble(), ticketName + " 구매");
        return "구매가 완료되었습니다.";
    }
}
