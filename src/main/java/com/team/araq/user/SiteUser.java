package com.team.araq.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
}
