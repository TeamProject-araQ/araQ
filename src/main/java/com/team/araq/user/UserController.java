package com.team.araq.user;

import com.team.araq.board.email.MailService;
import com.team.araq.report.BlacklistService;
import com.team.araq.sms.SmsService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final MailService mailService;
    private final SmsService smsService;
    private final BlacklistService blacklistService;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "user/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult, @RequestParam("image") MultipartFile image, Model model) {
        if (bindingResult.hasErrors()) {
            return "user/signup";
        }
        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지않습니다.");
            return "user/signup";
        }
        if (this.blacklistService.checkBlacklist(userCreateForm.getPhoneNum()) != null) {
            model.addAttribute("blacklist", this.blacklistService.checkBlacklist(userCreateForm.getPhoneNum()));
            return "user/blacklist";
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

    @GetMapping("/resetPw/{token}")
    public String resetPw(@PathVariable("token") String token, Model model){
        SiteUser user = userService.getByUserToken(token);
        model.addAttribute("user", user);
        return "/user/resetPw";
    }

    @PostMapping("/resetPw")
    public String resetPw(@RequestParam("token") String token, @RequestParam("newPw") String newPw, @RequestParam("confirmPw") String confirmPw){
        SiteUser user = userService.getByUserToken(token);
        if(user != null){
            userService.updatePw(user, newPw, confirmPw);
            mailService.createToken(user.getUsername());
            return "redirect:/user/login";
        }
        return "redirect:/error";
    }

    @PostMapping("/sendVerKey")
    @ResponseBody
    public String sendVerKey(@RequestBody Map<String, String> data, HttpSession session) {
        String name = data.get("name");
        String phoneNum = data.get("phoneNum");

        SiteUser user = userService.getByNameAndPhoneNum(name, phoneNum);

        if (user.getPhoneNum().equals(phoneNum)) {
            String verKey = smsService.createRandomNum();
            session.setAttribute("verKey", verKey);
            smsService.sendSms(phoneNum, verKey);
            return "success";
        }
        return "fail";
    }

    @PostMapping("/findId")
    @ResponseBody
    public String findId(@RequestBody Map<String, String> data, HttpSession session){
        String name = data.get("name");
        String phoneNum = data.get("phoneNum");
        String verKey = data.get("verKey");

        String storedVerKey = (String) session.getAttribute("verKey");

        if(verKey.equals(storedVerKey)){
            SiteUser user = userService.getByNameAndPhoneNum(name, phoneNum);
            String username = user.getUsername();
            return username;
        }
        return "fail";
    }

    @PostMapping("/sendVerificationCode")
    @ResponseBody
    public String sendVerificationCod(@RequestBody Map<String, String> data, HttpSession session) {
        String username = data.get("username");
        String phoneNum = data.get("phoneNum");

        SiteUser user = userService.getByUsername(username);

        if (user.getPhoneNum().equals(phoneNum)) {
            String verificationcode = smsService.createRandomNum();
            session.setAttribute("verificationCode", verificationcode);
            smsService.sendSms(phoneNum, verificationcode);
            return "success";
        } else {
            return "fail";
        }
    }

    @PostMapping("/verifyCode")
    @ResponseBody

    public ResponseEntity<Map<String, String>>verifyCode(@RequestBody Map<String, String> data, HttpSession session){
        String username = data.get("username");
        String phoneNum = data.get("phoneNum");
        String verificationCode = data.get("verificationCode");

        String verkey = (String) session.getAttribute("verificationCode");

        SiteUser user = userService.getByUsername(username);

        if(user != null && user.getPhoneNum().equals(phoneNum) && verificationCode.equals(verkey)){
            mailService.createToken(username);
            session.removeAttribute("verificationCode");
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("token", user.getToken());

            return ResponseEntity.ok(responseMap);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}