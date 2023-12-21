package com.team.araq.inquiry;

import com.team.araq.inquiry.answer.Answer;
import com.team.araq.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String category;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String status;

    private LocalDateTime createDate;

    private LocalDateTime modifyDate;

    @ManyToOne
    private SiteUser writer;

    @OneToOne(mappedBy = "inquiry", cascade = CascadeType.REMOVE)
    private Answer answer;
}
