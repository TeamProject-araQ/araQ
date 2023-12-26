package com.team.araq.report;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    private final UserService userService;

    @PostMapping("/create")
    public String create(String reason, String detailReason, String reportedUsername, Principal principal) {
        if (reason.equals("1")) reason = "채팅방 도배 및 광고";
        else if (reason.equals("2")) reason = "욕설 / 폭언 / 비속어 등";
        else if (reason.equals("3")) reason = "음란성 / 선정성";
        else if (reason.equals("4")) reason = "기타";
        SiteUser reportedUser = this.userService.getByUsername(reportedUsername);
        SiteUser reportingUser = this.userService.getByUsername(principal.getName());
        this.reportService.createReport(reason, detailReason, reportingUser, reportedUser);
        return "redirect:/";
    }
}