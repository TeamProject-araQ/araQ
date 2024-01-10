package com.team.araq.like;

import com.team.araq.chat.Notification;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/like")
public class LikeController {

    private final LikeService likeService;

    private final UserService userService;

    private final SimpMessagingTemplate simpMessagingTemplate;


    @PostMapping("/request")
    @ResponseBody
    public String like(@RequestBody String username, Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        SiteUser likedUser = this.userService.getByUsername(username);
        if (this.likeService.getListForCheck(likedUser, user) != null) {
            return "이미 " + likedUser.getNickName() + "님께 아라큐 요청을 받았습니다. 매칭 상태를 확인해주세요.";
        }
        else if (this.likeService.getListForCheck(user, likedUser) != null) {
            return "이미 " + likedUser.getNickName() + "님께 아라큐 요청을 보냈습니다. 매칭 상태를 확인해주세요.";
        } else if (user.getAraQPass() == 0) return "아라큐 신청권이 필요합니다.";
        this.likeService.likeUser(user, likedUser);
        this.userService.useAraQPass(user);
        Notification notification = new Notification("아라큐 요청", user.getNickName() + "님이 아라큐 요청을 보냈습니다.",
                user.getUsername(), username, "#");
        simpMessagingTemplate.convertAndSend("/topic/notification/" + username, notification);
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
