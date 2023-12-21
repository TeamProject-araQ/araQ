package com.team.araq.inquiry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Integer> {

    Page<Inquiry> findAll(Specification<Inquiry> spec, Pageable pageable);

}
