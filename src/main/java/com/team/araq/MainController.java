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
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Controller
public class MainController {
    private final UserService userService;
    private final PostService postService;
    private final Set<String> onlineUsers;


    @GetMapping("/")
    public String index(Principal principal, Model model) {
        if (principal != null) {
            SiteUser user = this.userService.getByUsername(principal.getName());
            model.addAttribute("user", user);
        }
        List<Post> postList = this.postService.getList();
        model.addAttribute("postList", postList);

        List<SiteUser> onlines = new ArrayList<>();

        for (String str : onlineUsers) onlines.add(userService.getByUsername(str));
        model.addAttribute("onlineUsers", onlines);

        return "index";
    }

    @GetMapping("/test")
    public String test(Model model) {
        return "test";
    }

    @MessageMapping("/online")
    public void online(MessageDto messageDto) {
        this.onlineUsers.add(messageDto.getTarget());
    }

    @PostMapping("/offline")
    @ResponseBody
    public String offline(@RequestBody String username) {
        this.onlineUsers.remove(username);
        return null;
    }


}
