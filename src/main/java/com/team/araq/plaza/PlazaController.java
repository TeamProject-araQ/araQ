package com.team.araq.plaza;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public String create(@ModelAttribute PlazaDto plazaDto, Principal principal) throws IOException {
        SiteUser user = userService.getByUsername(principal.getName());
        String uuid = UUID.randomUUID().toString();
        plazaDto.setManager(user);
        plazaDto.setCode(uuid);
        Plaza plaza = plazaService.create(plazaDto);

        if (plazaDto.getCustomImg().isEmpty()) plazaService.setImg(plaza, plazaDto.getImg());
        else {
            MultipartFile img = plazaDto.getCustomImg();
            String originalName = img.getOriginalFilename();
            assert originalName != null;
            String ext = originalName.substring(originalName.lastIndexOf('.'));
            String filename = uuid + ext;
            String dirPath = "uploads/plaza";
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dirPath + "/" + filename);
            FileCopyUtils.copy(img.getBytes(), file);

            plazaService.setImg(plaza, "/plaza/image/" + filename);
        }
        return "redirect:/plaza/list";
    }

    @PostMapping("/join")
    public String joinPlaza(Model model, @RequestParam("code") String code,
                            Principal principal) {
        Plaza plaza = plazaService.getByCode(code);
        model.addAttribute("plaza", plaza);

        if (plaza.getBlackList().contains(principal.getName())) {
            simpMessagingTemplate.convertAndSend("/topic/alert/" + principal.getName(),
                    "강제퇴장 당한 방에는 재입장 할 수 없습니다.");
            return "redirect:/plaza/list";
        } else if (plaza.getPeople() >= plaza.getMaxPeople()) {
            simpMessagingTemplate.convertAndSend("/topic/alert/" + principal.getName(),
                    "해당 광장의 정원이 가득 찼습니다.");
            return "redirect:/plaza/list";
        }

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
        Plaza plaza = plazaService.getByCode(code);
        if (plaza.getReported()) return "redirect:/plaza/list";

        File dir = new File("uploads/plaza");

        if (dir.exists()) {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                if (file.getName().contains(code))
                    file.delete();
            }
        }
        plazaService.delete(plaza);
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
    public String modify(@ModelAttribute PlazaDto plazaDto) throws IOException {
        Plaza plaza = plazaService.getByCode(plazaDto.getCode());
        if (plaza.getReported()) return "deny";
        String imgPath = null;
        plazaService.modify(plaza, plazaDto);

        if (plazaDto.getImg() != null) {
            imgPath = plazaDto.getImg();
        } else {
            MultipartFile img = plazaDto.getCustomImg();
            String originalName = img.getOriginalFilename();
            assert originalName != null;
            String ext = originalName.substring(originalName.lastIndexOf('.'));
            String filename = plazaDto.getCode() + ext;
            String dirPath = "uploads/plaza";
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dirPath + "/" + filename);
            FileCopyUtils.copy(img.getBytes(), file);
            imgPath = "/plaza/image/" + filename;
        }
        plazaService.setImg(plaza, imgPath);
        simpMessagingTemplate.convertAndSend("/topic/plaza/modify/" + plazaDto.getCode(), imgPath);

        return null;
    }

    @PostMapping("/delegate")
    @ResponseBody
    public String delegate(@RequestBody Map<String, String> data) {
        String code = data.get("code");
        String value = data.get("target");
        Plaza plaza = plazaService.getByCode(code);
        if (plaza.getReported()) return "deny";
        SiteUser target = userService.getByUsername(value);
        plazaService.changeManager(plaza, target);
        simpMessagingTemplate.convertAndSend("/topic/plaza/delegate/" + code + "/" + value, value);
        return null;
    }
}
