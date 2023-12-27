package com.team.araq.board.post;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {

    private final PostService postService;

    private final UserService userService;

    @GetMapping("/create")
    public String create() {
        return "board/write";
    }

    @PostMapping("/create")
    public String create(String content, Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        this.postService.createPost(content, user);
        return "redirect:/";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {
        Post post = this.postService.getPost(id);
        model.addAttribute("post", post);
        return "board/detail";
    }

    @GetMapping("/modify/{id}")
    public String modify(@PathVariable("id") Integer id, Model model, Principal principal) {
        Post post = this.postService.getPost(id);
        if (!post.getWriter().getUsername().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        model.addAttribute("post", post);
        return "board/write";
    }

    @PostMapping("/modify/{id}")
    public String modify(@PathVariable("id") Integer id, String content, Principal principal) {
        Post post = this.postService.getPost(id);
        if (!post.getWriter().getUsername().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        this.postService.modifyPost(post, content);
        return "redirect:/post/detail/" + post.getId();
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id, Principal principal) {
        Post post = this.postService.getPost(id);
        if (!post.getWriter().getUsername().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        this.postService.deletePost(post);
        return "redirect:/";
    }

}
