package com.team.araq.inquiry;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/inquiry")
public class InquiryController {

    private final InquiryService inquiryService;

    private final UserService userService;

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "kw", defaultValue = "") String kw) {
        Page<Inquiry> paging = this.inquiryService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "inquiry/list";
    }

    @GetMapping("/create")
    public String create(InquiryDTO inquiryDTO, Model model) {
        return "inquiry/write";
    }

    @PostMapping("/create")
    public String create(@Valid InquiryDTO inquiryDTO, BindingResult bindingResult, Principal principal,
                         @RequestParam(value = "files") MultipartFile[] files) throws IOException {
        SiteUser user = this.userService.getByUsername(principal.getName());
        Inquiry inquiry = this.inquiryService.createInquiry(inquiryDTO, user);
        this.inquiryService.uploadImage(inquiry, files);
        return "redirect:/inquiry/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Integer id, Model model, Principal principal) {
        Inquiry inquiry = this.inquiryService.getInquiry(id);
        if (!inquiry.getWriter().getUsername().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        model.addAttribute("inquiry", inquiry);
        return "inquiry/detail";
    }
}
