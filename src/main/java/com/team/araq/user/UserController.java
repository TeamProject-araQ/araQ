package com.team.araq.user;

import com.team.araq.board.email.MailService;
import com.team.araq.board.post.Post;
import com.team.araq.board.post.PostService;
import com.team.araq.chat.rate.Rate;
import com.team.araq.chat.rate.RateService;
import com.team.araq.inquiry.Inquiry;
import com.team.araq.inquiry.InquiryService;
import com.team.araq.pay.Payment;
import com.team.araq.pay.PaymentService;
import com.team.araq.sms.SmsService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private final InquiryService inquiryService;

    private final PostService postService;

    private final PaymentService paymentService;

    private final RateService rateService;

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
        if (userService.isPhoneNumTaken(userCreateForm.getPhoneNum())) {
            bindingResult.rejectValue("phoneNum", "phoneNumTaken", "이미 가입된 전화번호입니다.");
            return "user/signup";
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

    @PostMapping("/update")
    public String update(@RequestParam("username") String username, UserUpdateForm userUpdateForm, Principal principal, RedirectAttributes redirectAttributes) throws IOException {
        SiteUser user = userService.getByUsername(username);

        if (!userService.update(user, userUpdateForm)) {
            redirectAttributes.addFlashAttribute("updateError", "필수 항목을 모두 입력해주세요");
            return "redirect:/user/update";
        }
        userService.update(user, userUpdateForm);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<GrantedAuthority> auths = new ArrayList<>();
        auths.add(new SimpleGrantedAuthority(user.getRole().getValue()));

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                auth.getPrincipal(), auth.getCredentials(), auths
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);

        return "redirect:/";
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
        return "redirect:/";
    }

    @GetMapping("/updatePw")
    public String checkPw(Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        if (user.isSocialJoin())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return "user/updatePw";
    }

    @GetMapping("/out")
    public String out() {
        return "user/out";
    }

    @PostMapping("/out")
    public String out(Principal principal) {
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

    @PostMapping("/getRate")
    @ResponseBody
    public String getRate(@RequestBody String username) {
        SiteUser user = this.userService.getByUsername(username);
        List<Rate> rateList = this.rateService.getRateByUser(user);
        if (rateList.isEmpty()) return null;
        double manner = 0;
        double appeal = 0;
        double appearance = 0;
        for (int i = 0; i < rateList.size(); i++) {
            manner += rateList.get(i).getManner();
            appeal += rateList.get(i).getAppeal();
            appearance += rateList.get(i).getAppearance();
        }
        manner = Math.round(manner / rateList.size() * 10) / 10.0;
        appeal = Math.round(appeal / rateList.size() * 10) / 10.0;
        appearance = Math.round(appearance / rateList.size() * 10) / 10.0;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("manner", manner);
        jsonObject.put("appeal", appeal);
        jsonObject.put("appearance", appearance);
        return jsonObject.toString();
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
        model.addAttribute("target", user);
        model.addAttribute("user", user);
        return "user/resetPw";
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
    public String sendVerKey(@RequestBody String data, HttpSession session) {
        JSONObject jsonObject = new JSONObject(data);
        String name = jsonObject.get("name").toString();
        String phoneNum = jsonObject.get("phoneNum").toString();

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
    public String findId(@RequestBody String data, HttpSession session) {
        JSONObject jsonObject = new JSONObject(data);
        String name = jsonObject.get("name").toString();
        String phoneNum = jsonObject.get("phoneNum").toString();
        String verKey = jsonObject.get("verKey").toString();

        String storedVerKey = (String) session.getAttribute("verKey");

        if (verKey.equals(storedVerKey)) {
            SiteUser user = userService.getByNameAndPhoneNum(name, phoneNum);
            if (user.isSocialJoin()) return "소셜 로그인으로 가입된 회원입니다.";
            return "회원님의 아이디는 " + user.getUsername() + " 입니다.";
        }
        return "fail";
    }

    // 비밀번호 찾기시 문자로 인증번호 보내기
    @PostMapping("/sendVerificationCode")
    @ResponseBody
    public String sendVerificationCode(@RequestBody Map<String, String> data, HttpSession session) {
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
        List<Rate> rateList = this.rateService.getRateByUser(user);
        double manner = 0;
        double appeal = 0;
        double appearance = 0;
        for (int i = 0; i < rateList.size(); i++) {
            manner += rateList.get(i).getManner();
            appeal += rateList.get(i).getAppeal();
            appearance += rateList.get(i).getAppearance();
        }
        manner = Math.round(manner / rateList.size() * 10) / 10.0;
        appeal = Math.round(appeal / rateList.size() * 10) / 10.0;
        appearance = Math.round(appearance / rateList.size() * 10) / 10.0;
        model.addAttribute("manner", manner);
        model.addAttribute("appeal", appeal);
        model.addAttribute("appearance", appearance);
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
    public boolean checkAccess(Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        return user.isListenVoice();
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
    public ResponseEntity<String> checkUsername(@RequestParam String username) {
        if (userService.checkUsername(username)) {
            return ResponseEntity.ok("available");
        } else {
            return ResponseEntity.ok("unavailable");
        }
    }

    @GetMapping("/checkEmail")
    @ResponseBody
    public ResponseEntity<String> checkEmail(@RequestParam String email) {
        if (userService.checkEmail(email)) {
            return ResponseEntity.ok("available");
        } else {
            return ResponseEntity.ok("unavailable");
        }
    }

    @PostMapping("/check/chatPass")
    @ResponseBody
    public boolean checkChatPass(Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        if (user.getChatPass() == 0) return false;
        else return true;
    }

    @ResponseBody
    @PostMapping("/save/phone")
    public String savePhone(@RequestBody String phoneNum, Principal principal) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        if (userService.socialPhone(phoneNum).isPresent()) {
            userService.deleteUser(user);
            return "deny";
        }
        this.userService.savePhoneNum(user, phoneNum);
        return "휴대폰 인증이 완료되었습니다.";
    }


    @ResponseBody
    @PostMapping("/sendSocialVerKey")
    public String sendSocialVerkey(@RequestBody String phoneNum, Principal principal, HttpSession session) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        if (user.getPhoneNum().equals(phoneNum)) {
            String verKey = smsService.createRandomNum();
            session.setAttribute("verKey", verKey);
            smsService.sendSms(phoneNum, verKey);
            return "success";
        } else {
            return "fail";
        }
    }

    @PostMapping("/confirmSocialVerKey")
    @ResponseBody
    public String confirmSocialVerKey(@RequestBody String verKey, HttpSession session) {
        String storedVerKey = (String) session.getAttribute("verKey");
        if (verKey.equals(storedVerKey)) {
            return "success";
        }
        return "fail";
    }

    @GetMapping("/suspended")
    public String suspend(Principal principal, Model model) {
        SiteUser user = this.userService.getByUsername(principal.getName());
        LocalDateTime endTime = user.getSuspendedEndTime();
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, endTime);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        model.addAttribute("time", days + "일 " + hours + "시간");
        return "user/suspend";
    }

    @GetMapping("/banned")
    public String ban() {
        return "user/ban";
    }
}