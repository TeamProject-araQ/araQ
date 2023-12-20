package com.team.araq.user.admin;

import com.team.araq.board.post.Post;
import com.team.araq.board.post.PostService;
import com.team.araq.pay.Payment;
import com.team.araq.pay.PaymentService;
import com.team.araq.review.Review;

import com.team.araq.review.ReviewService;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    private final PostService postService;

    private final ReviewService reviewService;

    private final PaymentService paymentService;

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

    @GetMapping("/review")
    public String manageReview(Model model, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "kw", defaultValue = "") String kw) {
        Page<Review> paging = this.reviewService.getAll(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "admin/review";
    }

    @GetMapping("/payment")
    public String managePayment(Model model, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "kw", defaultValue = "") String kw) {
        Page<Payment> paging = this.paymentService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "admin/payment";
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

    @PostMapping("/review/delete")
    @ResponseBody
    public String deleteReview(@RequestBody List<String> reviews) {
        for (String reviewId : reviews) {
            Review review = this.reviewService.getReview(Integer.parseInt(reviewId));
            this.reviewService.deleteReview(review);
        }
        return "리뷰가 삭제되었습니다.";
    }
}
