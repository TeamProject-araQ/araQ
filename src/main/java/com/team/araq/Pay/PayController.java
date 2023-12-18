package com.team.araq.Pay;

import com.siot.IamportRestClient.IamportClient;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class PayController {

    @GetMapping("/pay")
    public String pay() {
        return "user/payment";
    }
}
