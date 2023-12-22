package com.team.araq.user;

import com.team.araq.board.email.MailDto;
import com.team.araq.board.email.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;


@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "user/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult, @RequestParam("image") MultipartFile image) {
        if (bindingResult.hasErrors()) {
            return "user/signup";
        }
        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지않습니다.");
            return "user/signup";
        }
        try {
            userService.create(userCreateForm, image);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "user/signup";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "user/signup";
        }

        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/update")
    public String modify() {
        return "user/update";
    }

    @PostMapping("/checkCurrentPw")
    public String checkCurrentPassword(@RequestParam("currentPassword") String currentPassword, Principal principal, Model model) {
        SiteUser user = userService.getByUsername(principal.getName());
        boolean checkedPw = userService.checkPassword(user, currentPassword);
        if (checkedPw) {
            return "user/changePw";
        } else {
            model.addAttribute("error", "현재 비밀번호가 올바르지 않습니다.");
            return "user/updatePw";
        }
    }

    @PostMapping("/changePw")
    public String changePw(@RequestParam("newPw") String newPw, @RequestParam("confirmPw") String confirmPw, Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        userService.updatePw(user, newPw, confirmPw);
        return "redirect:/user/update";
    }

    @GetMapping("/updatePw")
    public String checkPw() {
        return "user/updatePw";
    }


    @GetMapping("/out")
    public String out() {
        return "user/out";
    }

    @PostMapping("/out")
    public String out(@RequestParam("username") String username, @RequestParam("password") String password, Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        userService.deleteUser(user);
        SecurityContextHolder.getContext().setAuthentication(null);
        return "redirect:/user/logout";
    }

    @PostMapping("/checkUser")
    @ResponseBody
    public String checkUser(@RequestParam String username, @RequestParam String password, Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        if (userService.checkUser(user, username, password)) {
            return "success";
        } else {
            return "fail";
        }
    }

    @PostMapping("/getInfo")
    @ResponseBody
    public SiteUser getInfo(@RequestBody String username) {
        return userService.getByUsername(username);

    }

    @PostMapping("/findId")
    @ResponseBody
    public Map<String, Object> findId(@RequestBody String email){
        Map<String, Object> response = new HashMap<>();
        String username = userService.findUsernameByEmail(email).getUsername();

        if(username != null){
            response.put("success", true);
            response.put("username", username);
        } else {
            response.put("success", false);
        }
        return response;
    }

    @PostMapping("/sendEmail")
    @ResponseBody
    public String sendEmail(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String email = data.get("email");

        SiteUser user = userService.getByUsername(username);

        if (user.getEmail().equals(email)) {
            mailService.sendPasswordResetEmail(username);
            return "success";
        } else {
            return "fail";
        }
    }
}
