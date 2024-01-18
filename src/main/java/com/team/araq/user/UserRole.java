package com.team.araq.user;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    NEW("ROLE_NEW"),
    SUPER("ROLE_SUPER"),
    SUSPEND("ROLE_SUSPEND"),
    BAN("ROLE_BAN");

    UserRole(String value) {
        this.value = value;
    }

    private String value;
}