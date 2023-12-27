package com.team.araq;

import com.team.araq.board.post.Post;
import com.team.araq.board.post.PostService;
import com.team.araq.chat.MessageDto;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

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

        List<SiteUser> onlines = userService.getLoginUsers();

        model.addAttribute("onlineUsers", onlines);

        return "index";
    }

    @GetMapping("/test")
    public String test(Model model) {
        return "test";
    }

    @MessageMapping("/online")
    public void online(MessageDto messageDto) {
        SiteUser user = userService.getByUsername(messageDto.getTarget());
        this.userService.login(user);
    }

    @PostMapping("/offline")
    @ResponseBody
    public String offline(@RequestBody String username) {
        SiteUser user = userService.getByUsername(username);
        userService.logout(user);
        return null;
    }

    @GetMapping("/reset")
    public String reset(Principal principal) {
        if (!principal.getName().equals("njk7740"))
            throw new RuntimeException("권한이 없습니다.");
        userService.logout(userService.getLoginUsers());
        return "redirect:/";
    }


}
