package com.team.araq.idealType;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/idealType")
public class IdealTypeController {

    private final IdealTypeService idealTypeService;

    private final UserService userService;

    @GetMapping("/create")
    public String create(IdealTypeDTO idealTypeDTO) {
        return "user/idealType";
    }

    @PostMapping("/create")
    public String create(@Valid IdealTypeDTO idealTypeDTO, BindingResult bindingResult, Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        if (bindingResult.hasErrors())
            return "user/idealType";
        this.idealTypeService.createIdealType(user, idealTypeDTO);
        return "redirect:/user/page";
    }

    @GetMapping("/modify")
    public String modify(IdealTypeDTO idealTypeDTO, Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        IdealType idealType = this.idealTypeService.getIdealTypeByUser(user);
        idealTypeDTO.setEducation(idealType.getEducation());
        idealTypeDTO.setDrinking(idealType.getDrinking());
        idealTypeDTO.setMaxAge(idealType.getMaxAge());
        idealTypeDTO.setMinAge(idealType.getMinAge());
        idealTypeDTO.setReligion(idealType.getReligion());
        idealTypeDTO.setSmoking(idealType.getSmoking());
        idealTypeDTO.setMaxHeight(idealType.getMaxHeight());
        idealTypeDTO.setMinHeight(idealType.getMinHeight());
        return "user/idealType";
    }

    @PostMapping("/modify")
    public String modify(@Valid IdealTypeDTO idealTypeDTO, BindingResult bindingResult, Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        IdealType idealType = this.idealTypeService.getIdealTypeByUser(user);
        if (bindingResult.hasErrors())
            return "user/idealType";
        this.idealTypeService.modifyIdealType(idealType, idealTypeDTO);
        return "redirect:/user/page";
    }
}
