package com.team.araq.match;

import com.team.araq.like.LikeService;
import com.team.araq.like.UserLike;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchController {

    private final UserService userService;

    private final LikeService likeService;

    @GetMapping("/around")
    public String around(Model model, Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        String address = user.getAddress();
        String[] words = address.split(" ");
        String gender = user.getGender().equals("남성") ? "여성" : "남성";
        List<SiteUser> userList = userService.getByAddress(words[0] + " " + words[1], gender);
        String addressSplit = words[0] + " " + words[1];
        List<UserLike> likeList = this.likeService.getListByUser(user);
        Map<String, String> likesStatus = new HashMap<>();
        for (SiteUser siteUser : userList) {
            String status = likeService.checkStatus(user, siteUser);
            likesStatus.put(siteUser.getUsername(), status);
        }
        model.addAttribute("userList", userList);
        model.addAttribute("likeList", likeList);
        model.addAttribute("likesStatus", likesStatus);
        model.addAttribute("address", addressSplit);
        return "conn/match";
    }

    @GetMapping("/personality")
    public String personality() {
        return "conn/match";
    }

}
