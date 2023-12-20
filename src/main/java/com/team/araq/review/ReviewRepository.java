package com.team.araq.review;

import com.team.araq.board.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Page<Review> findAll(Pageable pageable);

    Page<Review> findAll(Specification<Review> spec, Pageable pageable);
}
