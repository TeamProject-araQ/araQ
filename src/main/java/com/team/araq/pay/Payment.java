package com.team.araq.pay;

import com.team.araq.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String orderNum;

    private LocalDateTime date;

    @ManyToOne
    private SiteUser user;

    private int amount;

    private String method;

    private String status;

}
