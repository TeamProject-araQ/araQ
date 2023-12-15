package com.team.araq.board.Post;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public List<Post> getList() {
        return this.postRepository.findAll();
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
