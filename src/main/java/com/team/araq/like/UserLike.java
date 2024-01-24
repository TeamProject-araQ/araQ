package com.team.araq.like;

import com.team.araq.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class UserLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private SiteUser user;

    @ManyToOne
    private SiteUser likedUser;

    private String status;

    private LocalDateTime successDate;

    private LocalDateTime failDate;
}
