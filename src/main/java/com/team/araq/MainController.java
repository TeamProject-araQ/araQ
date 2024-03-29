package com.team.araq;

import com.team.araq.board.post.Post;
import com.team.araq.board.post.PostService;
import com.team.araq.like.LikeService;
import com.team.araq.like.UserLike;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserRepository;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    private final UserRepository userRepository;

    @GetMapping("/")
    public String index(Principal principal, Model model) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        List<Post> postList = this.postService.getList();
        List<SiteUser> onlines = userService.getLoginUsers();
        onlines.remove(user);
        List<UserLike> likeList = this.likeService.getListByUser(user);
        List<SiteUser> userList = this.userService.getListByPreference(user.getGender());
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
    @ResponseBody
    public String test(Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        long beforeTime = System.currentTimeMillis();
        List<SiteUser> users = userService.getByPersonalities(user);
        long afterTime = System.currentTimeMillis(); // 코드 실행 후에 시간 받아오기
        long secDiffTime = afterTime - beforeTime; //두 시간에 차 계산
        System.out.println("시간차이: " + secDiffTime);
        return "test";
    }

    @GetMapping("/reset")
    public String reset(Principal principal) {
        if (!principal.getName().equals("njk7740"))
            throw new RuntimeException("권한이 없습니다.");
        userService.logout(userService.getLoginUsers());
        return "redirect:/";
    }

    @GetMapping("/onlyAdmin")
    public String onlyAdmin() {
        return "error/onlyAdmin";
    }
}
