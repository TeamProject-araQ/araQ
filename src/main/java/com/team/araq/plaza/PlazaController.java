package com.team.araq.plaza;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/plaza")
public class PlazaController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;
    private final PlazaService plazaService;

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("plazaList", plazaService.getAll());
        model.addAttribute("alarm", "");
        return "plaza/list";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute PlazaDto plazaDto, Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        String uuid = UUID.randomUUID().toString();
        plazaDto.setManager(user);
        plazaDto.setCode(uuid);
        plazaService.create(plazaDto);
        return "redirect:/plaza/list";
    }

    @PostMapping("/join")
    public String joinPlaza(Model model, @RequestParam("code") String code,
                            Principal principal) {
        Plaza plaza = plazaService.getByCode(code);
        model.addAttribute("plaza", plaza);
        if (plaza.getPeople() >= plaza.getMaxPeople()) {
            model.addAttribute("plazaList", plazaService.getAll());
            model.addAttribute("alarm", "해당 광장의 정원이 가득 찼습니다.");
            return "plaza/list";
        };
        SiteUser user = userService.getByUsername(principal.getName());
        userService.setLocation(user, code);
        userService.setUserLocationInPlaza(user, "0px", "0px");
        userService.setFocusInPlaza(user, "focus");
        List<SiteUser> joinUsers = userService.getByLocation(code);
        model.addAttribute("onlinePlazaUsers", joinUsers);
        return "plaza/plaza";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("code") String code) {
        plazaService.delete(plazaService.getByCode(code));
        return "redirect:/plaza/list";
    }

    @PostMapping("/check")
    @ResponseBody
    public String check(@RequestBody Map<String, String> data) {
        Plaza plaza = plazaService.getByCode(data.get("code"));
        if (plaza.getPassword().equals(data.get("input"))) return "access";
        return "deny";
    }

    @PostMapping("/modify")
    @ResponseBody
    public String modify(@RequestBody Map<String, String> data) {
        Plaza plaza = plazaService.getByCode(data.get("code"));
        plazaService.modify(plaza, data.get("title"), data.get("password"), Integer.valueOf(data.get("people")));
        return null;
    }
}
