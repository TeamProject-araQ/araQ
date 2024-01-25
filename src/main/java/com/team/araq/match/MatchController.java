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
import java.util.*;

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
        List<SiteUser> userList = userService.getByAddress(words[0] + " " + words[1], user.getGender());
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
        return "conn/around";
    }

    @GetMapping("/idealType")
    public String idealType(Principal principal, Model model) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        List<SiteUser> userList = this.userService.getByIdealType(user.getIdealType(), user.getGender());
        List<UserLike> likeList = this.likeService.getListByUser(user);
        Map<String, String> likesStatus = new HashMap<>();
        for (SiteUser siteUser : userList) {
            String status = likeService.checkStatus(user, siteUser);
            likesStatus.put(siteUser.getUsername(), status);
        }
        model.addAttribute("userList", userList);
        model.addAttribute("likeList", likeList);
        model.addAttribute("likesStatus", likesStatus);
        return "conn/idealType";
    }

    @GetMapping("/personalityType")
    public String personalityType(Principal principal, Model model) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        List<SiteUser> smokingList = this.userService.getBySmoking(user.getGender(), user.getSmoking());
        List<SiteUser> drinkingList = this.userService.getByDrinking(user.getGender(), user.getDrinking());
        List<SiteUser> hobbyList = this.userService.getByHobby(user.getGender(), user.getHobby());
        List<SiteUser> mbtiList = this.userService.getByMbti(user.getGender(), user.getMbti());
//        List<SiteUser> personalityList = this.userService.getByPersonalities(user);
        List<SiteUser> religionList = this.userService.getByReligion(user.getGender(), user.getReligion());
        List<UserLike> likeList = this.likeService.getListByUser(user);
        Set<SiteUser> uniqueUsers = new HashSet<>();
        uniqueUsers.addAll(smokingList);
        uniqueUsers.addAll(drinkingList);
        uniqueUsers.addAll(hobbyList);
        uniqueUsers.addAll(mbtiList);
        uniqueUsers.addAll(religionList);
//        uniqueUsers.addAll(personalityList);
        Map<String, String> likesStatus = new HashMap<>();
        for (SiteUser siteUser : uniqueUsers) {
            String status = likeService.checkStatus(user, siteUser);
            likesStatus.put(siteUser.getUsername(), status);
        }
        model.addAttribute("religionList", religionList);
        model.addAttribute("smokingList", smokingList);
        model.addAttribute("drinkingList", drinkingList);
        model.addAttribute("hobbyList", hobbyList);
        model.addAttribute("mbtiList", mbtiList);
//        model.addAttribute("personalityList", personalityList);
        model.addAttribute("likeList", likeList);
        model.addAttribute("likesStatus", likesStatus);
        return "conn/personalityType";
    }

    @GetMapping("/random")
    public String random(Principal principal, Model model) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        List<UserLike> likeList = this.likeService.getListByUser(user);
        List<SiteUser> userList = this.userService.getRandomList(user.getGender());
        Map<String, String> likesStatus = new HashMap<>();
        for (SiteUser siteUser : userList) {
            String status = likeService.checkStatus(user, siteUser);
            likesStatus.put(siteUser.getUsername(), status);
        }
        model.addAttribute("userList", userList);
        model.addAttribute("likeList", likeList);
        model.addAttribute("likesStatus", likesStatus);
        return "conn/random";
    }

}
