package com.team.araq.board.post;

import com.team.araq.user.SiteUser;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    private Specification<Post> search(String kw) {
        return new Specification<Post>() {
            @Override
            public Predicate toPredicate(Root<Post> post, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                query.distinct(true);
                Join<Post, SiteUser> u1 = post.join("writer", JoinType.LEFT);
                return criteriaBuilder.or(criteriaBuilder.like(post.get("content"), "%" + kw + "%"),
                        criteriaBuilder.like(u1.get("nickName"), "%" + kw + "%"));
            }
        };
    }

    public List<Post> getList() {
        return this.postRepository.findAll();
    }

    public Page<Post> getList(int page, String kw) {
        List<Sort.Order> sort = new ArrayList<>();
        sort.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sort));
        Specification<Post> specification = search(kw);
        return this.postRepository.findAll(specification, pageable);
    }

    public Post getPost(Integer id) {
        Optional<Post> post = this.postRepository.findById(id);
        if (post.isPresent()) return post.get();
        else throw new RuntimeException("그런 게시물 없습니다.");
    }

    public void createPost(String content, SiteUser user) {
        Post post = new Post();
        post.setContent(content);
        post.setWriter(user);
        post.setCreateDate(LocalDateTime.now());
        this.postRepository.save(post);
    }

    public void modifyPost(Post post, String content) {
        post.setContent(content);
        post.setModifyDate(LocalDateTime.now());
        this.postRepository.save(post);
    }

    public void deletePost(Post post) {
        this.postRepository.delete(post);
    }
}
