package com.team.araq.user;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private String uploadPath = "C:/uploads/user";



    private Specification<SiteUser> search(String kw) {
        return new Specification<SiteUser>() {
            @Override
            public Predicate toPredicate(Root<SiteUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                query.distinct(true);
                return criteriaBuilder.or(criteriaBuilder.like(root.get("username"), "%" + kw + "%"),
                        criteriaBuilder.like(root.get("nickName"), "%" + kw + "%"));
            }
        };
    }


    public SiteUser create(UserCreateForm userCreateForm, MultipartFile image) throws IOException {
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
        user.setCreateDate(LocalDateTime.now());
        user.setGender(userCreateForm.getGender());
        user.setIntroduce(userCreateForm.getIntroduce());

        File uploadDirectory = new File(uploadPath);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }
        String fileExtension = StringUtils.getFilenameExtension(image.getOriginalFilename());
        String fileName = user.getUsername() + "." + fileExtension;
        File dest = new File(uploadPath + File.separator + fileName);
        FileCopyUtils.copy(image.getBytes(), dest);
        user.setImage("/user/image/" + user.getUsername() + "." + fileExtension);
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

    public void plusBubbles(SiteUser user, int bubble) {
        user.setBubble(user.getBubble() + bubble);
        this.userRepository.save(user);
    }

    public void minusBubbles(SiteUser user, int bubble) {
        user.setBubble(user.getBubble() - bubble);
        this.userRepository.save(user);
    }



    public Page<SiteUser> getList(int page, String kw) {
        List<Sort.Order> sort = new ArrayList<>();
        sort.add(Sort.Order.asc("username"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sort));
        Specification<SiteUser> specification = search(kw);
        return this.userRepository.findAll(specification, pageable);
    }

    public void createAdmin() {
        SiteUser user = new SiteUser();
        user.setUsername("admin");
        user.setNickName("관리자");
        user.setPassword(passwordEncoder.encode("admin"));
        this.userRepository.save(user);
    }

    public void deleteUser(SiteUser user) {
        this.userRepository.delete(user);
    }

    public boolean checkPassword(SiteUser user,String currentPassword) {
        if(passwordEncoder.matches(currentPassword, user.getPassword())){
            return true;
        } else {
            return false;
        }
    }

    public void updatePw(SiteUser user, String newPw, String confirmPw) {
        if(newPw.equals(confirmPw)){
            user.setPassword(passwordEncoder.encode(newPw));
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("새로운 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }
    }
}
