package com.team.araq.review;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    private final UserService userService;

    @GetMapping("/list")
    public String list() {
        return "review/reviewList";
    }

    /*

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "sort") String sort) {
        Page<Review> paging = this.reviewService.getList(page, sort);
        model.addAttribute("paging", paging);
        model.addAttribute("sort", sort);
        return "review/reviewList";
    }

     */

    @GetMapping("/create")
    public String create(ReviewDTO reviewDTO) {
        return "review/writeReview";
    }

    @PostMapping("/create")
    public String create(@Valid ReviewDTO reviewDTO, Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        this.reviewService.createReview(reviewDTO, user);
        return "redirect:/review/list";
    }
}
