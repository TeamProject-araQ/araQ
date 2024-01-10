package com.team.araq.plaza;

import com.team.araq.user.SiteUser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlazaDto {
    private String title;
    private Integer people;
    private String password;
    private String code;
    private SiteUser manager;
    private String img;
}
