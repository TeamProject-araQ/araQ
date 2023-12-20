package com.team.araq.review;

import com.team.araq.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String answer1;

    @Column(columnDefinition = "TEXT")
    private String answer2;

    @Column(columnDefinition = "TEXT")
    private String answer3;

    @Column(columnDefinition = "TEXT")
    private String answer4;

    @Column(columnDefinition = "TEXT")
    private String answer5;

    @OneToOne
    private SiteUser writer;

    private LocalDateTime createDate;

    private LocalDateTime modifyDate;

    private double star;

    private String image;

}
