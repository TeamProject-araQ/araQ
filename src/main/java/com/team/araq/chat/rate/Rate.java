package com.team.araq.chat.rate;

import com.team.araq.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Rate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private SiteUser user1;

    @ManyToOne
    private SiteUser user2;

    private double manner;

    private double appeal;

    private double appearance;

    private LocalDateTime createDate;

}
