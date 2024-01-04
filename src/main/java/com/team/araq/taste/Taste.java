package com.team.araq.taste;

import com.team.araq.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Taste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private SiteUser user;

    private String option1;

    private String option2;

    private String option3;

    private String option4;

    private String option5;

    private String option6;

    private String option7;

    private String option8;

    private String option9;

    private String option10;

}
