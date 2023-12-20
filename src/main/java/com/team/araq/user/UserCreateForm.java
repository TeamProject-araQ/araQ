package com.team.araq.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class UserCreateForm {
    @NotEmpty(message= "아이디를 입력하세요.")
    private String username;
    @NotEmpty(message= "비밀번호를 입력하세요.")
    private String password1;
    @NotEmpty(message= "비밀번호를 확인해주세요.")
    private String password2;
    @NotEmpty(message= "이메일을 입력하세요.")
    @Email
    private String email;
    @NotEmpty(message= "핸드폰 번호를 입력하세요.")
    private String phoneNum;

    private String nickName;

    private String address;

    private String age;

    private String height;

    // 종교
    private String religion;

    // 음주
    private String drinking;

    // 흡연
    private String smoking;

    // 학력
    private String education;

    // mbti
    private String mbti;

    // 성격
    private List<String> personality;

    // 취미
    private String hobby;

    // 사진
    private MultipartFile image;

    // 성별
    private String gender;

    // 자기소개
    private String introduce;
}
