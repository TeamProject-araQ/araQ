package com.team.araq.pay;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final UserService userService;

    public void savePayment(PaymentDTO paymentDTO) {
        Payment pay = new Payment();
        pay.setDate(paymentDTO.getDate());
        pay.setUser(this.userService.getByUsername(paymentDTO.getUsername()));
        pay.setAmount(paymentDTO.getAmount());
        pay.setMethod(paymentDTO.getMethod());
        pay.setOrderNum(paymentDTO.getOrderNum());
        pay.setDate(LocalDateTime.now());
        pay.setStatus("결제 완료");
        this.paymentRepository.save(pay);
    }
}
