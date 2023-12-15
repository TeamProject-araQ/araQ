package com.team.araq.board.Post;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    private final UserService userService;

    @GetMapping("/post/create")
    public String create() {
        return "board/writePost";
    }

    @PostMapping("/post/create")
    public String create(String content, Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        this.postService.createPost(content, user);
        return "redirect:/";
    }

}
