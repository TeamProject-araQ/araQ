package com.team.araq.board.comment;

import com.team.araq.board.post.Post;
import com.team.araq.board.post.PostService;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    private final PostService postService;

    private final UserService userService;

    @PostMapping("/create/{id}")
    public String create(@PathVariable("id") Integer id, String content, Principal principal) {
        Post post = this.postService.getPost(id);
        SiteUser user = this.userService.getByUsername(principal.getName());
        this.commentService.createComment(content, post, user);
        return "redirect:/post/detail/" + post.getId();
    }

    @PostMapping("/modify/{id}")
    public String modify(@PathVariable("id") Integer id, String content, Principal principal) {
        Comment comment = this.commentService.getComment(id);
        if (!comment.getWriter().getUsername().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        this.commentService.modifyComment(comment, content);
        return "redirect:/post/detail/" + comment.getPost().getId();
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id, Principal principal) {
        Comment comment = this.commentService.getComment(id);
        if (!comment.getWriter().getUsername().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        this.commentService.deleteComment(comment);
        return "redirect:/post/detail/" + comment.getPost().getId();
    }
}
