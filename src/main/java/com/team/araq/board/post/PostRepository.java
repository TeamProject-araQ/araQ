package com.team.araq.board.post;

import com.team.araq.inquiry.Inquiry;
import com.team.araq.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {

    Page<Post> findAll(Specification<Post> spec, Pageable pageable);

    List<Post> findTop3ByWriterOrderByCreateDateDesc(SiteUser writer);

}
