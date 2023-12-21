package com.team.araq.inquiry;

import com.team.araq.board.post.Post;
import com.team.araq.user.SiteUser;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    private Specification<Inquiry> search(String kw) {
        return new Specification<Inquiry>() {
            @Override
            public Predicate toPredicate(Root<Inquiry> inquiry, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                query.distinct(true);
                Join<Inquiry, SiteUser> u1 = inquiry.join("writer", JoinType.LEFT);
                return criteriaBuilder.or(criteriaBuilder.like(inquiry.get("content"), "%" + kw + "%"),
                        criteriaBuilder.like(inquiry.get("title"), "%" + kw + "%"),
                        criteriaBuilder.like(u1.get("nickName"), "%" + kw + "%"));
            }
        };
    }

    public Page<Inquiry> getList(int page, String kw) {
        List<Sort.Order> sort = new ArrayList<>();
        sort.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sort));
        Specification<Inquiry> spec = search(kw);
        return this.inquiryRepository.findAll(spec, pageable);
    }
}
