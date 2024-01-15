package com.team.araq.user.oauth2;

import com.team.araq.user.SiteUser;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String id;

    public OAuthAttributes(String id, Map<String, Object> attributes, String nameAttributeKey, String name, String email) {
        this.id = id;
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
    }

    public OAuthAttributes() {
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if (registrationId.equals("kakao")) {
            return ofKakao(userNameAttributeName, attributes);
        } else if (registrationId.equals("github")) return ofGithub(userNameAttributeName, attributes);
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakao_account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakao_account.get("profile");

        return new OAuthAttributes(attributes.get("id").toString(),
                attributes,
                userNameAttributeName,
                (String) profile.get("nickname"),
                (String) kakao_account.get("email"));
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {

        return new OAuthAttributes(attributes.get("sub").toString(),
                attributes,
                userNameAttributeName,
                (String) attributes.get("name"),
                (String) attributes.get("email"));
    }

    private static OAuthAttributes ofGithub(String userNameAttributeName, Map<String, Object> attributes) {
        return new OAuthAttributes(attributes.get("id").toString(),
                attributes,
                userNameAttributeName,
                (String) attributes.get("name"),
                (String) attributes.get("email"));
    }

    public SiteUser toEntity() {
        String id = null;
        if (nameAttributeKey.equals("response")) {
            id = attributes.get("id").toString();
        } else id = attributes.get(nameAttributeKey).toString();
        return new SiteUser(id, name, email, true, LocalDateTime.now());
    }
}