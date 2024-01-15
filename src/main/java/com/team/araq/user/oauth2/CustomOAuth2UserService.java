package com.team.araq.user.oauth2;

import com.team.araq.user.SiteUser;
import com.team.araq.user.UserRepository;
import com.team.araq.user.UserRole;
import edu.emory.mathcs.backport.java.util.Collections;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2UserService oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(oAuth2UserRequest);

        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();

        String userNameAttributeName = oAuth2UserRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        SiteUser user = saveOrUpdate(attributes);
        httpSession.setAttribute("siteUser", new SessionUser(user));

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(user.getRole().getValue())), attributes.getAttributes(), attributes.getNameAttributeKey());

    }

    private SiteUser saveOrUpdate(OAuthAttributes attributes) {
        Optional<SiteUser> userByEmail = userRepository.findByEmail(attributes.getEmail());
        if (userByEmail.isPresent()) {
            SiteUser user = userByEmail.get();
            if (user.isSocialJoin() && user.getUsername().equals(attributes.getId())) {
                return user.update(attributes.getName(), attributes.getEmail());
            }
            OAuth2Error oauth2Error = new OAuth2Error("email_already_registered", "이미 가입된 이메일 주소입니다.", null);
            throw new OAuth2AuthenticationException(oauth2Error);
        }
        Optional<SiteUser> userByUsername = userRepository.findByusername(attributes.getId());
        if (userByUsername.isPresent()) {
            SiteUser user = userByUsername.get();
            return user.update(attributes.getName(), attributes.getEmail());
        } else {
            SiteUser newUser = attributes.toEntity();
            newUser.setRole(UserRole.NEW);
            return userRepository.save(newUser);
        }
    }
}
