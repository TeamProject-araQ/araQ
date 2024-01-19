package com.team.araq.report;

import com.team.araq.chat.Chat;
import com.team.araq.chat.Room;
import com.team.araq.chat.RoomService;
import com.team.araq.plaza.Plaza;
import com.team.araq.plaza.PlazaService;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    private final UserService userService;

    private final RoomService roomService;

    private final PlazaService plazaService;

    @PostMapping("/create")
    public String create(String reason, String detailReason, String reportedUsername, String reportedRoomCode,
                         String reportedLocation, Principal principal) {
        if (reason.equals("1")) reason = "채팅방 도배 및 광고";
        else if (reason.equals("2")) reason = "욕설 / 폭언 / 비속어 등";
        else if (reason.equals("3")) reason = "음란성 / 선정성";
        else if (reason.equals("4")) reason = "기타";
        SiteUser reportedUser = this.userService.getByUsername(reportedUsername);
        SiteUser reportingUser = this.userService.getByUsername(principal.getName());

        if (reportedLocation.equals("chat"))
            roomService.setReport(roomService.get(reportedRoomCode), true);
        else if (reportedLocation.equals("plaza"))
            plazaService.setReport(plazaService.getByCode(reportedRoomCode), true);

        this.reportService.createReport(reason, detailReason, reportingUser, reportedUser, reportedRoomCode, reportedLocation);
        return "redirect:/";
    }

    @PostMapping("/getChatHistory")
    @ResponseBody
    public List<Chat> getChatHistory(@RequestBody String code) {
        Room room = roomService.get(code);
        return room.getChats();
    }

    @PostMapping("/getPlazaHistory")
    @ResponseBody
    public Plaza getPlazaHistory(@RequestBody String code) {
        return plazaService.getByCode(code);
    }
}
