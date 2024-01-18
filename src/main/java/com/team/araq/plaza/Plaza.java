package com.team.araq.plaza;

import com.team.araq.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Plaza {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String code;
    private String title;
    @ManyToOne
    private SiteUser manager;
    private Integer maxPeople;
    private Integer People;
    private LocalDateTime createDate;
    private String password;
    private String background;
    private Boolean reported;
}
