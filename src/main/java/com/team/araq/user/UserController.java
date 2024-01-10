package com.team.araq.user;

import com.team.araq.board.email.MailService;
import com.team.araq.board.post.Post;
import com.team.araq.board.post.PostService;
import com.team.araq.inquiry.Inquiry;
import com.team.araq.inquiry.InquiryService;
import com.team.araq.pay.Payment;
import com.team.araq.pay.PaymentService;
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

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private final MailService mailService;

    private final SmsService smsService;

    private final BlacklistService blacklistService;

    private final InquiryService inquiryService;

    private final PostService postService;

    private final PaymentService paymentService;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "user/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult, Model model) {
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
            SiteUser user = userService.create(userCreateForm);
            model.addAttribute("user", user);
            model.addAttribute("userUpdateForm", new UserUpdateForm());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "user/signup";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "user/signup";
        }

        return "redirect:/user/login";
    }

    @PostMapping("/update")
    public String update(@RequestParam("username") String username, UserUpdateForm userUpdateForm) throws IOException {
        SiteUser user = userService.getByUsername(username);
        userService.update(user, userUpdateForm);
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/update")
    public String update(Model model, Principal principal, UserUpdateForm userUpdateForm) {
        SiteUser user = userService.getByUsername(principal.getName());
        model.addAttribute("user", user);
        return "user/updateInfo";
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
    public String resetPw(@PathVariable("token") String token, Model model) {
        SiteUser user = userService.getByUserToken(token);
        model.addAttribute("user", user);
        return "/user/resetPw";
    }

    @PostMapping("/resetPw")
    public String resetPw(@RequestParam("token") String token, @RequestParam("newPw") String newPw, @RequestParam("confirmPw") String confirmPw) {
        SiteUser user = userService.getByUserToken(token);
        if (user != null) {
            userService.updatePw(user, newPw, confirmPw);
            mailService.createToken(user.getUsername());
            return "redirect:/user/login";
        }
        return "redirect:/error";
    }

    //회원 가입시 문자 인증번호 보내기
    @PostMapping("/signupAuth")
    @ResponseBody
    public String signupAuth(@RequestBody Map<String, String> data, HttpSession session) {
        String phoneNum = data.get("phoneNum").trim();
        if (phoneNum.length() != 11 || phoneNum == null) {
            return "fail";
        }
        String verKey = smsService.createRandomNum();
        System.out.println(verKey);
        session.setAttribute("verKey", verKey);
        smsService.sendSms(phoneNum, verKey);
        return "success";
    }

    // 회원 가입시 문자 인증번호 확인
    @PostMapping("/confirmPhoneNum")
    @ResponseBody
    public String confirmPhoneNum(@RequestBody Map<String, String> data, HttpSession session) {
        String phoneNum = data.get("phoneNum");
        String verKey = data.get("verKey");

        String storedVerKey = (String) session.getAttribute("verKey");
        if (verKey.equals(storedVerKey)) {
            return "success";
        }
        return "fail";
    }

    // 아이디 찾기시 문자인증 번호 보내기
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


    // 아이디 찾기시 문자 인증번호 확인 후 아이디 보여주기
    @PostMapping("/findId")
    @ResponseBody
    public String findId(@RequestBody Map<String, String> data, HttpSession session) {
        String name = data.get("name");
        String phoneNum = data.get("phoneNum");
        String verKey = data.get("verKey");

        String storedVerKey = (String) session.getAttribute("verKey");

        if (verKey.equals(storedVerKey)) {
            SiteUser user = userService.getByNameAndPhoneNum(name, phoneNum);
            String username = user.getUsername();
            return username;
        }
        return "fail";
    }

    // 비밀번호 찾기시 문자로 인증번호 보내기
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

    // 비밀번호 찾기시 문자 인증번호 확인
    @PostMapping("/verifyCode")
    @ResponseBody
    public ResponseEntity<Map<String, String>> verifyCode(@RequestBody Map<String, String> data, HttpSession session) {
        String username = data.get("username");
        String phoneNum = data.get("phoneNum");
        String verificationCode = data.get("verificationCode");

        String verkey = (String) session.getAttribute("verificationCode");

        SiteUser user = userService.getByUsername(username);

        if (user != null && user.getPhoneNum().equals(phoneNum) && verificationCode.equals(verkey)) {
            mailService.createToken(username);
            session.removeAttribute("verificationCode");
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("token", user.getToken());

            return ResponseEntity.ok(responseMap);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/page")
    public String page(Principal principal, Model model) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        List<Inquiry> inquiryList = this.inquiryService.getListByWriter(user);
        List<Post> postList = this.postService.getListByWriter(user);
        List<Payment> paymentList = this.paymentService.getListByUser(user);
        model.addAttribute("inquiryList", inquiryList);
        model.addAttribute("postList", postList);
        model.addAttribute("paymentList", paymentList);
        return "user/page";
    }

    @GetMapping("/post")
    public String post() {

        return "user/post";
    }

    @GetMapping("/inquiry")
    public String inquiry() {
        return "user/inquiry";
    }

    @GetMapping("/payment")
    public String payment() {
        return "user/payment";
    }

    @GetMapping("/edit")
    public String edit() {
        return "user/edit";
    }

    @PostMapping("/edit")
    public String edit(UserUpdateForm userUpdateForm, Principal principal) throws IOException {
        SiteUser user = userService.getByUsername(principal.getName());
        userService.edit(user, userUpdateForm);
        return "redirect:/";
    }

    @ResponseBody
    @PostMapping("/record")
    public String record(@RequestParam("audio") MultipartFile multipartFile, Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        try {
            this.userService.uploadAudio(multipartFile, user);
            return "녹음이 완료되었습니다.";
        } catch (Exception e) {
            return "녹음 파일 업로드 실패" + e.getMessage();
        }
    }


    @ResponseBody
    @PostMapping("/deleteImage")
    public String deleteImage(@RequestBody String imageUrl, Principal principal) {
        try {
            SiteUser user = userService.getByUsername(principal.getName());
            List<String> images = user.getImages();
            userService.deleteImage(images, imageUrl, user);
            return "이미지가 성공적으로 삭제되었습니다.";
        } catch (Exception e) {
            return "이미지 삭제에 실패했습니다.";
        }
    }

    @ResponseBody
    @PostMapping("/setProfileImage")
    public String setProfileImage(@RequestBody String imageUrl, Principal principal) {
        try {
            SiteUser user = userService.getByUsername(principal.getName());
            userService.setProfileImage(imageUrl, user);
            return "이미지가 성공적으로 수정되었습니다.";
        } catch (Exception e) {
            return "이미지 수정에 실패했습니다.";
        }
    }

    @ResponseBody
    @PostMapping("/addImage")
    public ResponseEntity<?> addImage(@RequestParam("image") MultipartFile image, Principal principal) {
        try {
            SiteUser user = userService.getByUsername(principal.getName());
            List<String> images = userService.addImage(image, user);

            return ResponseEntity.ok().body(images);
        } catch (Exception e) {
            e.printStackTrace(); // 또는 로그에 기록 등을 수행할 수 있음
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 업로드 실패");
        }
    }

    @PostMapping("/checkAccess")
    @ResponseBody
    public boolean checkAccess(Principal principal, @RequestBody String username) {
        SiteUser user1 = this.userService.getByUsername(principal.getName());
        return user1.getOpenVoice().stream()
                .anyMatch(user2 -> user2.getUsername().equals(username));
    }

    @GetMapping("/personality")
    @ResponseBody
    public ResponseEntity<List<String>> getUserPersonality(Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        List<String> userPersonality = userService.getUserPersonality(user);
        return ResponseEntity.ok(userPersonality);
    }

    @PostMapping("/savePersonality")
    @ResponseBody
    public String savePersonality(@RequestBody List<String> personality, Principal principal) {
        SiteUser user = userService.getByUsername(principal.getName());
        userService.savePersonality(user, personality);
        return "success";
    }

    @GetMapping("/checkUsername")
    @ResponseBody
    public ResponseEntity<String> checkUsername(@RequestParam String username){
        if(userService.checkUsername(username)){
            return ResponseEntity.ok("available");
        } else{
            return ResponseEntity.ok("unavailable");
        }
    }

    @GetMapping("/checkEmail")
    @ResponseBody
    public ResponseEntity<String> checkEmail(@RequestParam String email){
        if(userService.checkEmail(email)){
            return ResponseEntity.ok("available");
        } else {
            return ResponseEntity.ok("unavailable");
        }
    }
}