package com.team.araq.user.oauth2;

import com.mysql.cj.Session;
import com.team.araq.user.SiteUser;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


public class SessionUser implements Serializable {
    private String name;
    private String email;


    public SessionUser(SiteUser siteUser){
        this.name = siteUser.getName();
        this.email = siteUser.getEmail();
    }

    public SessionUser() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}