package com.team.araq.pay;

import com.team.araq.review.Review;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final UserService userService;

    private Specification<Payment> search(String kw) {
        return new Specification<Payment>() {
            @Override
            public Predicate toPredicate(Root<Payment> payment, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                query.distinct(true);
                Join<Payment, SiteUser> u1 = payment.join("user", JoinType.LEFT);
                return criteriaBuilder.or(criteriaBuilder.like(payment.get("orderNum"), "%" + kw + "%"),
                        criteriaBuilder.like(u1.get("nickName"), "%" + kw + "%"),
                        criteriaBuilder.like(u1.get("username"), "%" + kw + "%"));
            }
        };
    }

    public Page<Payment> getList(int page, String kw) {
        List<Sort.Order> sort = new ArrayList<>();
        sort.add(Sort.Order.desc("date"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sort));
        Specification<Payment> specification = search(kw);
        return this.paymentRepository.findAll(specification, pageable);
    }

    public void savePayment(PaymentDTO paymentDTO) {
        Payment pay = new Payment();
        pay.setDate(paymentDTO.getDate());
        pay.setUser(this.userService.getByUsername(paymentDTO.getUsername()));
        pay.setAmount(paymentDTO.getAmount());
        pay.setMethod(paymentDTO.getMethod());
        pay.setOrderNum(paymentDTO.getOrderNum());
        pay.setDate(LocalDateTime.now());
        pay.setCard(paymentDTO.getCard());
        pay.setStatus("결제 완료");
        this.paymentRepository.save(pay);
    }
}
