package com.team.araq.pay;

import com.team.araq.inquiry.Inquiry;
import com.team.araq.review.Review;
import com.team.araq.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Page<Payment> findAll(Specification<Payment> spec, Pageable pageable);

    Optional<Payment> findByImpUid(String impUid);

    List<Payment> findTop3ByUserOrderByDateDesc(SiteUser user);
}
