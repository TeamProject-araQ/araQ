package com.team.araq;

import com.team.araq.board.Post.Post;
import com.team.araq.board.Post.PostService;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class MainController {

    private final UserService userService;

    private final PostService postService;

    @GetMapping("/")
    public String index(Principal principal, Model model) {
        if (principal != null) {
            SiteUser user = this.userService.getByUsername(principal.getName());
            model.addAttribute("user", user);
        }
        List<Post> postList = this.postService.getList();
        model.addAttribute("postList", postList);
        return "index";
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }


}
