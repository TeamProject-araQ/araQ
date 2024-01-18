package com.team.araq.user.admin;

import com.team.araq.announcement.Announcement;
import com.team.araq.announcement.AnnouncementService;
import com.team.araq.board.post.Post;
import com.team.araq.board.post.PostService;
import com.team.araq.inquiry.Inquiry;
import com.team.araq.inquiry.InquiryService;
import com.team.araq.pay.Payment;
import com.team.araq.pay.PaymentService;
import com.team.araq.report.BlacklistService;
import com.team.araq.report.Report;
import com.team.araq.report.ReportService;
import com.team.araq.review.Review;
import com.team.araq.review.ReviewService;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserRole;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.struts.chain.commands.UnauthorizedActionException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    private final PostService postService;

    private final ReviewService reviewService;

    private final PaymentService paymentService;

    private final InquiryService inquiryService;

    private final ReportService reportService;

    private final BlacklistService blacklistService;

    private final AnnouncementService announcementService;

    @GetMapping("")
    public String page() {
        return "admin/page";
    }

    @GetMapping("/user")
    public String manageUser(Model model, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "kw", defaultValue = "") String kw, Principal principal) {
        Page<SiteUser> paging = this.userService.getList(page, kw);
        SiteUser admin = userService.getByUsername(principal.getName());
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("admin", admin);
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

    @GetMapping("/inquiry")
    public String manageInquiry(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "kw", defaultValue = "") String kw,
                                @RequestParam(value = "category", defaultValue = "") String category) {
        Page<Inquiry> paging = this.inquiryService.getList(page, kw, category);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("category", category);
        return "admin/inquiry";
    }

    @GetMapping("/report")
    public String manageReport(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        Page<Report> paging = this.reportService.getList(page);
        model.addAttribute("paging", paging);
        return "admin/report";
    }

    @GetMapping("/announcement")
    public String manageAnnouncement(@RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "kw", defaultValue = "") String kw, Model model) {
        Page<Announcement> paging = this.announcementService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "admin/announcement";
    }

    @ResponseBody
    @PostMapping("/announcement/delete")
    public String deleteAnnouncement(@RequestBody List<String> announcements) {
        for (String announcementId : announcements) {
            Announcement announcement = this.announcementService.getAnnouncement(Integer.parseInt(announcementId));
            this.announcementService.deleteAnnouncement(announcement);
        }
        return "공지 사항이 삭제되었습니다.";
    }

    @PostMapping("/report/delete")
    @ResponseBody
    public String deleteReport(@RequestBody List<String> reports) {
        for (String reportId : reports) {
            Report report = this.reportService.getReport(Integer.parseInt(reportId));
            this.reportService.deleteReport(report);
        }
        return "신고 내역이 삭제되었습니다.";
    }

    @PostMapping("/user/delete")
    @ResponseBody
    public String deleteUser(@RequestBody List<String> usernames) {
        for (String username : usernames) {
            SiteUser user = this.userService.getByUsername(username);
            if (user.getRole().equals(UserRole.ADMIN))
                return "관리자는 삭제할 수 없습니다.";
        }
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

    @PostMapping("/cancel")
    @ResponseBody
    public String cancelPayment(@RequestBody List<String> payments) throws Exception {
        for (String impUid : payments) {
            Payment payment = this.paymentService.getPayment(impUid);
            if (payment.getStatus().equals("cancelled"))
                return "이미 취소된 결제입니다.";
            else if (payment.getUser().getBubble() < payment.getAmount())
                return "이미 사용된 결제 내역입니다.";
            else {
                this.paymentService.cancelPayment(impUid);
                this.paymentService.updatePayment(payment, impUid);
                this.userService.minusBubbles(payment.getUser(), payment.getAmount());
            }
        }
        return "결제가 취소되었습니다.";
    }

    @ResponseBody
    @PostMapping("/inquiry/delete")
    public String deleteInquiry(@RequestBody List<String> inquiries) {
        for (String inquiryId : inquiries) {
            Inquiry inquiry = this.inquiryService.getInquiry(Integer.parseInt(inquiryId));
            this.inquiryService.deleteInquiry(inquiry);
        }
        return "문의가 삭제되었습니다.";
    }

    @ResponseBody
    @PostMapping("/report/keep")
    public String keepUser(@RequestBody String reportId) {
        Report report = this.reportService.getReport(Integer.parseInt(reportId));
        this.reportService.updateStatus(report);
        return "처리가 완료되었습니다.";
    }

    @ResponseBody
    @PostMapping("/report/withdraw")
    public String withdrawUser(@RequestBody String reportId) {
        Report report = this.reportService.getReport(Integer.parseInt(reportId));
        if (blacklistService.checkBlacklist(report.getReportedUser().getPhoneNum()) == null) {
            this.blacklistService.saveBlacklist(report.getReportedUser().getPhoneNum(), report.getReason());
        }
        this.userService.deleteUser(report.getReportedUser());
        return "탈퇴 처리가 완료되었습니다.";
    }

    @GetMapping("/announcement/create")
    public String createAnnouncement() {
        return "announcement/write";
    }

    @PostMapping("/announcement/create")
    public String createAnnouncement(String title, String content) {
        this.announcementService.createAnnouncement(title, content);
        return "redirect:/admin/announcement";
    }

    @GetMapping("/announcement/modify/{id}")
    public String modify(@PathVariable("id") Integer id, Model model) {
        Announcement announcement = this.announcementService.getAnnouncement(id);
        model.addAttribute("announcement", announcement);
        return "announcement/write";
    }

    @PostMapping("/announcement/modify/{id}")
    public String modify(@PathVariable("id") Integer id, String title, String content) {
        Announcement announcement = this.announcementService.getAnnouncement(id);
        this.announcementService.modifyAnnouncement(announcement, title, content);
        return "redirect:/admin";
    }

    @PostMapping("/grantRole")
    public String grantRole(@RequestParam String username, @RequestParam UserRole role, Principal principal) throws UnauthorizedActionException {
        SiteUser admin = userService.getByUsername(principal.getName());
        SiteUser targetUser = userService.getByUsername(username);
        userService.saveRole(admin, targetUser, role);
        return "redirect:/admin/user";
    }
}
