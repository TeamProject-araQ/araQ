package com.team.araq.user.admin;

import com.team.araq.announcement.Announcement;
import com.team.araq.announcement.AnnouncementService;
import com.team.araq.board.post.Post;
import com.team.araq.board.post.PostService;
import com.team.araq.chat.RoomService;
import com.team.araq.inquiry.Inquiry;
import com.team.araq.inquiry.InquiryService;
import com.team.araq.pay.Payment;
import com.team.araq.pay.PaymentService;
import com.team.araq.plaza.PlazaService;
import com.team.araq.report.Report;
import com.team.araq.report.ReportService;
import com.team.araq.review.Review;
import com.team.araq.review.ReviewService;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserRole;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.struts.chain.commands.UnauthorizedActionException;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

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

    private final AnnouncementService announcementService;

    private final RoomService roomService;

    private final PlazaService plazaService;

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
    @PostMapping("/report/action")
    public String reportAction(@RequestBody Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Integer reportId = Integer.parseInt(entry.getKey());
            Map<String, Object> actionData = (Map<String, Object>) entry.getValue();
            String action = actionData.get("action").toString();
            Report report = this.reportService.getReport(reportId);
            SiteUser user = report.getReportedUser();
            String reason = actionData.get("reason").toString();
            if ("활동 유지".equals(action)) {
                this.reportService.updateStatus(report);
            } else if ("계정 정지".equals(action)) {
                int days = Integer.parseInt(String.valueOf(actionData.get("days")));
                this.userService.suspendUser(user, days, reason);
                this.reportService.updateStatus(report);
            } else if ("영구 정지".equals(action)) {
                this.userService.vanUser(user, reason);
                this.reportService.updateStatus(report);
            }
            if (report.getLocation().equals("chat")) roomService.setReport(roomService.get(report.getCode()), false);
            else if (report.getLocation().equals("plaza")) plazaService.setReport(plazaService.getByCode(report.getCode()), false);
        }
        return "조치가 완료되었습니다.";
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
    @ResponseBody
    public String grantRole(@RequestBody String data, Principal principal) throws
            UnauthorizedActionException {
        System.out.println(data);
        SiteUser admin = userService.getByUsername(principal.getName());
        JSONObject jsonObject = new JSONObject(data);
        SiteUser targetUser = userService.getByUsername(jsonObject.getString("username"));
        UserRole role = jsonObject.getEnum(UserRole.class, "role");
        userService.saveRole(admin, targetUser, role);
        return "권한이 변경되었습니다.";
    }
}
