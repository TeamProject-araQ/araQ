package com.team.araq.inquiry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InquiryRepository extends JpaRepository<Inquiry, Integer> {

    @Query("SELECT i FROM Inquiry i LEFT JOIN i.writer w WHERE " +
            "(LOWER(i.title) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(i.content) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(w.nickName) LIKE LOWER(CONCAT('%', :kw, '%'))) AND " +
            "(i.category = :category OR :category IS NULL OR :category = '')")
    Page<Inquiry> findByKeywordAndCategory(@Param("kw") String kw,
                                           @Param("category") String category,
                                           Pageable pageable);


}
