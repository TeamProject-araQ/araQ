package com.team.araq.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm){
        return "user/signup";
    }
    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult, @RequestParam("image") MultipartFile image){
        if(bindingResult.hasErrors()){
            return "user/signup";
        }
        if(!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())){
            bindingResult.rejectValue("password2","passwordInCorrect","2개의 패스워드가 일치하지않습니다.");
            return "user/signup";
        }
        try{
            userService.create(userCreateForm, image);
        }catch (DataIntegrityViolationException e){
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "user/signup";
        } catch(Exception e) {
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
    public String modify(){
        return "user/update";
    }

    @PostMapping("/checkCurrentPw")
    public Map<String, Boolean> checkCurrentPw(@RequestBody Map<String, String> request, Principal principal){
        String currentPassword = request.get("currentPassword");
        SiteUser user = userService.getByUsername(principal.getName());
        // 예시로 현재 비밀번호가 "test123"일 때 true를 반환하도록 설정합니다.
        boolean isValid = user != null && passwordEncoder.matches(currentPassword,user.getPassword());

        Map<String, Boolean> response = new HashMap<>();
        response.put("isValid", isValid);
        return response;
    }

    @GetMapping("/updatePw")
    public String checkPw(){
        return "user/updatePw";
    }
}
