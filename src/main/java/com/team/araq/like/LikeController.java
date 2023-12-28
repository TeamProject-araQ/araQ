package com.team.araq.like;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/like")
public class LikeController {

    private final LikeService likeService;

    private final UserService userService;


    @PostMapping("/request")
    @ResponseBody
    public String like(@RequestBody String username, Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        SiteUser likedUser = this.userService.getByUsername(username);
        if (!this.likeService.getListForCheck(likedUser, user).isEmpty()) {
            return "이미 " + likedUser.getNickName() + "님께 아라큐 요청을 받았습니다. 매칭 상태를 확인해주세요.";
        }
        else if (!this.likeService.getListForCheck(user, likedUser).isEmpty()) {
            return "이미 " + likedUser.getNickName() + "님께 아라큐 요청을 보냈습니다. 매칭 상태를 확인해주세요.";
        }
        this.likeService.likeUser(user, likedUser);
        return "성공적으로 요청되었습니다.";
    }

    @GetMapping("/accept/{id}")
    public String accept(@PathVariable("id") Integer id) {
        UserLike like = this.likeService.getLike(id);
        this.likeService.acceptUser(like);
        return "redirect:/";
    }

    @GetMapping("/refuse/{id}")
    public String refuse(@PathVariable("id") Integer id) {
        UserLike like = this.likeService.getLike(id);
        this.likeService.refuseLike(like);
        return "redirect:/";
    }
}
