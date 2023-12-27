package com.team.araq.announcement;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/announcement")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping("/list")
    public String list(@RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "kw", defaultValue = "") String kw, Model model) {
        Page<Announcement> paging = this.announcementService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "announcement/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {
        Announcement announcement = this.announcementService.getAnnouncement(id);
        this.announcementService.updateView(announcement);
        model.addAttribute("announcement", announcement);
        return "announcement/detail";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        Announcement announcement = this.announcementService.getAnnouncement(id);
        this.announcementService.deleteAnnouncement(announcement);
        return "redirect://announcement/list";
    }
}
