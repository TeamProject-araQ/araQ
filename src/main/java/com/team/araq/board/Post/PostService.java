package com.team.araq.board.Post;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

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
