package com.team.araq;

import com.team.araq.board.post.Post;
import com.team.araq.board.post.PostService;
import com.team.araq.chat.MessageDto;
import com.team.araq.like.LikeService;
import com.team.araq.like.UserLike;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class MainController {

    private final UserService userService;

    private final PostService postService;

    private final LikeService likeService;

    @GetMapping("/")
    public String index(Principal principal, Model model) {
        List<Post> postList = this.postService.getList();
        List<SiteUser> onlines = userService.getLoginUsers();
        SiteUser user = this.userService.getByUsername(principal.getName());
        List<UserLike> likeList = this.likeService.getListByUser(user);
        List<SiteUser> userList = this.userService.getRandomList(user.getGender());
        Map<String, String> likesStatus = new HashMap<>();
        for (SiteUser siteUser : userList) {
            String status = likeService.checkStatus(user, siteUser);
            likesStatus.put(siteUser.getUsername(), status);
        }
        model.addAttribute("postList", postList);
        model.addAttribute("onlineUsers", onlines);
        model.addAttribute("userList", userList);
        model.addAttribute("likeList", likeList);
        model.addAttribute("likesStatus", likesStatus);
        return "index";
    }

    @GetMapping("/test")
    public String test(Model model, Principal principal) {
        model.addAttribute("username", principal.getName());
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

    @GetMapping("/YStest")
    public String YStest() {
        return "YStest";
    }

}
