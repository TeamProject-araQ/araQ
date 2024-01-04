package com.team.araq.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class UserUpdateForm {
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

    // 대표 사진
    private MultipartFile image;

    // 성별
    private String gender;

    // 자기소개
    private String introduce;

    // 사진
    private List<MultipartFile> images;
}
