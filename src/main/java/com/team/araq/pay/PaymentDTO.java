package com.team.araq.pay;

import com.team.araq.user.SiteUser;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentDTO {

    private String orderNum;

    private LocalDateTime date;

    private int amount;

    private String method;

    private String username;
}
