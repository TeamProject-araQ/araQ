package com.team.araq.idealType;

import com.team.araq.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class IdealType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private SiteUser user;

    private int minAge;

    private int maxAge;

    private String drinking;

    private String education;

    private int minHeight;

    private int maxHeight;

    private String smoking;

    private String religion;
}
