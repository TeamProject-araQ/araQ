package com.team.araq.pay;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JsonBackReference
    private SiteUser user;

    private int amount;

    private String method;

    private String status;

    private String card;

    private String impUid;

    private String pg;

    private LocalDateTime cancelDate;

    // 충전액
    private int bubble;

}
