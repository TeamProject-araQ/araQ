package com.team.araq.user;

import com.team.araq.inquiry.Inquiry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class UserMailService {

    @Value("${spring.mail.username}")
    private String from;

    private final JavaMailSender javaMailSender;

    private final TemplateEngine templateEngine;

    public void sendMail(SiteUser user, Inquiry inquiry) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        Context context = new Context();
        context.setVariable("nickname", user.getNickName());
        context.setVariable("inquiryId", inquiry.getId());
        String emailContent = templateEngine.process("inquiry/mail", context);
        try {
            helper.setTo(user.getEmail());
            helper.setFrom(from);
            helper.setSubject("[AraQ] 문의 내용에 대한 답변 등록 안내");
            helper.setText(emailContent, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
