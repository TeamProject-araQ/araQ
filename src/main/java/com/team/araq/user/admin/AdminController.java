package com.team.araq.user.admin;

import com.team.araq.board.post.Post;
import com.team.araq.board.post.PostService;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    private final PostService postService;

    @GetMapping("")
    public String page() {
        return "admin/page";
    }

    @GetMapping("/user")
    public String manageUser(Model model, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "kw", defaultValue = "") String kw) {
        Page<SiteUser> paging = this.userService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "admin/user";
    }

    @GetMapping("/post")
    public String managePost(Model model, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "kw", defaultValue = "") String kw) {
        Page<Post> paging = this.postService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "admin/post";
    }

    @PostMapping("/user/delete")
    @ResponseBody
    public String deleteUser(@RequestBody List<String> usernames) {
        for (String username : usernames) {
            SiteUser user = this.userService.getByUsername(username);
            this.userService.deleteUser(user);
        }
        return "회원 정보가 삭제되었습니다.";
    }

    @PostMapping("/post/delete")
    @ResponseBody
    public String deletePost(@RequestBody List<String> posts) {
        for (String postId : posts) {
            Post post = this.postService.getPost(Integer.parseInt(postId));
            this.postService.deletePost(post);
        }
        return "게시물이 삭제되었습니다.";
    }
}
