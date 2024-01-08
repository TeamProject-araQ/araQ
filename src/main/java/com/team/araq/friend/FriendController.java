package com.team.araq.friend;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/friend")
public class FriendController {

    private final UserService userService;

    private final FriendService friendService;

    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/friend/request")
    public void requestFriend(@Payload String username) {
        JSONObject jsonObject = new JSONObject(username);
        SiteUser sender = this.userService.getByUsername((String) jsonObject.get("sender"));
        SiteUser receiver = this.userService.getByUsername((String) jsonObject.get("receiver"));
        Friend friend = this.friendService.checkFriend(sender, receiver);
        if (friend == null) {
            messagingTemplate.convertAndSend("/topic/friend/request/possible/" + receiver.getUsername(), username);
            this.friendService.requestFriend(sender, receiver);
        } else {
            if (friend.isStatus())
                messagingTemplate.convertAndSend("/topic/friend/request/impossible/" + sender.getUsername(),
                        receiver.getNickName() + "님이 이미 친구 목록에 존재합니다.");
            else messagingTemplate.convertAndSend("/topic/friend/request/impossible/" + sender.getUsername(),
                    receiver.getNickName() + "님이 이미 친구 요청 목록에 존재합니다.");
        }

    }

    @ResponseBody
    @PostMapping("/accept")
    public String acceptRequest(@RequestBody String username) {
        System.out.println(username);
        JSONObject jsonObject = new JSONObject(username);
        SiteUser receiver = this.userService.getByUsername((String) jsonObject.get("receiver"));
        SiteUser sender = this.userService.getByUsername((String) jsonObject.get("sender"));
        Friend friend = this.friendService.getFriend(sender, receiver);
        this.friendService.acceptFriend(friend);
        return sender.getNickName() + "님이 친구로 등록되었습니다.";
    }

    @GetMapping("/list")
    public String friendList(Principal principal, Model model) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        List<Friend> friendList = this.friendService.getList(user);
        List<Friend> requestList = this.friendService.requestList(user);
        model.addAttribute("requestList", requestList);
        model.addAttribute("friendList", friendList);
        return "user/friend";
    }

    @GetMapping("/delete/{username}")
    public String deleteFriend(@PathVariable("username") String username, Principal principal) {
        SiteUser user1 = this.userService.getByUsername(username);
        SiteUser user2 = this.userService.getByUsername(principal.getName());
        Friend friend = this.friendService.getFriend(user1, user2);
        this.friendService.refuseOrDeleteFriend(friend);
        return "redirect:/friend/list";
    }
}
