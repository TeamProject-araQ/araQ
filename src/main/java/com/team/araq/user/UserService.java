package com.team.araq.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SiteUser create(UserCreateForm userCreateForm) {
        SiteUser user = new SiteUser();
        user.setUsername(userCreateForm.getUsername());
        user.setEmail(userCreateForm.getEmail());
        user.setPassword(passwordEncoder.encode(userCreateForm.getPassword1()));
        user.setNickName(userCreateForm.getNickName());
        user.setPhoneNum(userCreateForm.getPhoneNum());
        user.setAddress(userCreateForm.getAddress());
        user.setAge(userCreateForm.getAge());
        user.setHeight(userCreateForm.getHeight());
        user.setReligion(userCreateForm.getReligion());
        user.setDrinking(userCreateForm.getDrinking());
        user.setSmoking(userCreateForm.getSmoking());
        user.setEducation(userCreateForm.getEducation());
        user.setMbti(userCreateForm.getMbti());
        user.setPersonality(userCreateForm.getPersonality());
        user.setHobby(userCreateForm.getHobby());
        user.setImage(userCreateForm.getImage());
        userRepository.save(user);
        return user;
    }

    public SiteUser getByUsername(String username) {
        Optional<SiteUser> user = userRepository.findByusername(username);
        if (user.isPresent()) return user.get();
        throw new RuntimeException("그런 사람 없습니다.");
    }

    public List<SiteUser> getByAddress(String address, String gender) {
        return userRepository.findByAddressLikeAndGender(address + "%", gender);
    }

    public void createTmp(String name, String age, String phoneNum, String address, String gender) {
        SiteUser user = new SiteUser();
        user.setNickName(name);
        user.setAge(age);
        user.setPhoneNum(phoneNum);
        user.setAddress(address);
        user.setGender(gender);
        userRepository.save(user);
    }
}
