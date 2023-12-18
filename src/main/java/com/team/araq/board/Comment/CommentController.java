package com.team.araq.board.Comment;

import com.team.araq.board.Post.Post;
import com.team.araq.board.Post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    private final PostService postService;

    @PostMapping("/create/{id}")
    public String create(@PathVariable("id") Integer id, String content) {
        Post post = this.postService.getPost(id);
        this.commentService.createComment(content, post);
        return "redirect:/post/detail/" + post.getId();
    }
}
