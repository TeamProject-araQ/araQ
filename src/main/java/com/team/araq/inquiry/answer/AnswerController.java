package com.team.araq.inquiry.answer;

import com.team.araq.inquiry.Inquiry;
import com.team.araq.inquiry.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/answer")
public class AnswerController {

    private final AnswerService answerService;

    private final InquiryService inquiryService;

    @PostMapping("/create/{id}")
    public String create(@PathVariable("id") Integer id, String content) {
        Inquiry inquiry = this.inquiryService.getInquiry(id);
        this.answerService.createAnswer(inquiry, content);
        this.inquiryService.updateStatus(inquiry);
        return "redirect:/admin/inquiry";
    }

}
