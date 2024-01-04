package com.team.araq.review;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    private final UserService userService;

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "sort", defaultValue = "createDate") String sort) {
        Page<Review> paging = this.reviewService.getList(page, sort);
        model.addAttribute("paging", paging);
        model.addAttribute("sort", sort);
        return "review/list";
    }

    @GetMapping("/create")
    public String create(ReviewDTO reviewDTO) {
        return "review/write";
    }

    @PostMapping("/create")
    public String create(@Valid ReviewDTO reviewDTO, Principal principal, MultipartFile image) throws IOException {
        SiteUser user = this.userService.getByUsername(principal.getName());
        Review review = this.reviewService.createReview(reviewDTO, user);
        this.reviewService.uploadImage(review, image);
        return "redirect:/review/list";
    }

    @GetMapping("/modify/{id}")
    public String modify(@PathVariable("id") Integer id, Principal principal, ReviewDTO reviewDTO) {
        Review review = this.reviewService.getReview(id);
        if (!review.getWriter().getUsername().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        reviewDTO.setAnswer1(review.getAnswer1());
        reviewDTO.setAnswer2(review.getAnswer2());
        reviewDTO.setAnswer3(review.getAnswer3());
        reviewDTO.setAnswer4(review.getAnswer4());
        reviewDTO.setAnswer5(review.getAnswer5());
        reviewDTO.setStar(review.getStar());

        return "review/write";
    }

    @PostMapping("/modify/{id}")
    public String modify(@PathVariable("id") Integer id, Principal principal, @Valid ReviewDTO reviewDTO, MultipartFile image) throws IOException {
        Review review = this.reviewService.getReview(id);
        if (!review.getWriter().getUsername().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        this.reviewService.modifyReview(review, reviewDTO);
        this.reviewService.uploadImage(review, image);
        return "redirect:/review/list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id, Principal principal) {
        Review review = this.reviewService.getReview(id);
        if (!review.getWriter().getUsername().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        this.reviewService.deleteReview(review);
        return "redirect:/review/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {
        Review review = this.reviewService.getReview(id);
        model.addAttribute("review", review);
        return "review/detail";
    }
}
