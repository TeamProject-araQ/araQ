package com.team.araq.board.comment;

import com.team.araq.board.post.Post;
import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public Comment getComment(Integer id) {
        Optional<Comment> comment = this.commentRepository.findById(id);
        if (comment.isPresent()) return comment.get();
        else throw new RuntimeException("그런 댓글 없습니다.");
    }

    public void createComment(String content, Post post, SiteUser user) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setWriter(user);
        comment.setCreateDate(LocalDateTime.now());
        comment.setPost(post);
        this.commentRepository.save(comment);
    }

    public void modifyComment(Comment comment, String content) {
        comment.setContent(content);
        comment.setModifyDate(LocalDateTime.now());
        this.commentRepository.save(comment);
    }

    public void deleteComment(Comment comment) {
        this.commentRepository.delete(comment);
    }
}
