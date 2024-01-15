package com.team.araq.user;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    NEW("ROLE_NEW");

    UserRole(String value) {
        this.value = value;
    }

    private String value;
}