package com.team.araq.user;

import com.team.araq.board.Comment.Comment;
import com.team.araq.board.Post.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class SiteUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 아이디
    @Column(unique = true)
    private String username;

    private String password;

    // 별명
    private String nickName;

    @Column(unique = true)
    private String email;

    private String phoneNum;

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
    private String image;

    @OneToMany(mappedBy = "writer", cascade = CascadeType.REMOVE)
    private List<Post> postList;

    @OneToMany(mappedBy = "writer", cascade = CascadeType.REMOVE)
    private List<Comment> commentList;

    private int bubble;

    private String gender;

    private String introduce;

}
