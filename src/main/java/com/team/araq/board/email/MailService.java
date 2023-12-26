package com.team.araq.board.email;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserRepository;
import com.team.araq.user.UserService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    public String getAuthNum(){
        char[] charSet = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
        String str = "";

        // 문자 배열 길이의 값을 랜덤으로 10개를 뽑아 구문을 작성함
        int idx = 0;
        for (int i = 0; i < 10; i++) {
            idx = (int) (charSet.length * Math.random());
            str += charSet[idx];
        }
        return str;
    }

    public MailDto createMail(String email) {
        String str = getAuthNum();
        MailDto dto = new MailDto();
        dto.setAddress(email);
        dto.setTitle("araQ 인증링크입니다.");
        dto.setMessage("회원님의 인증링크는 " + str + "입니다.");
        return dto;
    }

    public void mailSend(MailDto mailDto) {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        try {
            helper.setTo(mailDto.getAddress());
            helper.setSubject(mailDto.getTitle());

            // HTML 형식의 이메일 내용 설정
            helper.setText(mailDto.getMessage(), true);

            helper.setFrom("teriyaki970326@google.com");
            helper.setReplyTo("teriyaki970326@google.com");

            System.out.println("message: " + message);

            mailSender.send(message);
        } catch (Exception e) {
            // 예외 처리
            e.printStackTrace();
        }
    }

    public String createToken(String username) {
        String resetToken = getAuthNum(); // 랜덤 토큰 생성
        SiteUser user = userService.getByUsername(username);
        user.setToken(resetToken);
        userRepository.save(user);
        return resetToken;
    }

    public void sendPasswordResetEmail(String username) {
        SiteUser user = userService.getByUsername(username);
        String resetToken = createToken(username);

        // 비밀번호 재설정 링크를 이메일에 포함시켜 전송
        String resetLink = "http://localhost:8080/user/resetPw/" + resetToken;
        String emailContent = "<p>비밀번호를 변경하려면 다음 링크를 클릭하세요: <a href='" + resetLink + "'>비밀번호 변경하기</a></p>";
        MailDto dto = new MailDto();
        dto.setAddress(user.getEmail());
        dto.setTitle("[araQ] 비밀번호 재설정 안내");
        dto.setMessage(emailContent);

        mailSend(dto);
    }
}
