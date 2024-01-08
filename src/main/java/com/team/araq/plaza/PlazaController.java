package com.team.araq.plaza;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/plaza")
public class PlazaController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;
    private final PlazaService plazaService;

    @GetMapping("/join")
    public String join(Model model, Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        userService.setUserLocationInPlaza(user, "0px", "0px");
        userService.setFocusInPlaza(user, "focus");
        model.addAttribute("onlinePlazaUsers", userService.getOnlineInPlaza());
        return "plaza/plaza";
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("plazaList", plazaService.getAll());
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

    @GetMapping("/join/{code}")
    public String joinPlaza(Model model, @PathVariable("code") String code, Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        userService.setUserLocationInPlaza(user, "0px", "0px");
        userService.setFocusInPlaza(user, "focus");
        model.addAttribute("onlinePlazaUsers", userService.getOnlineInPlaza());
        Plaza plaza = plazaService.getByCode(code);
        model.addAttribute("plaza", plaza);
        return "plaza/plaza";
    }
}
