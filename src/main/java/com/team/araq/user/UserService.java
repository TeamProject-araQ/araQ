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
        // 파일 경로 폴더가 없을 때 폴더 생성
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        // 원본 파일에서 파일 확장자만 꺼내오기
        String fileExtension = StringUtils.getFilenameExtension(image.getOriginalFilename());

        // 파일 이름 정하고 확장자 뒤에 붙이기  (파일 이름 겹치면 안돼서 고유 아이디랑 합침.)
        String fileName = user.getUsername() + "." + fileExtension;

        // 파일의 전체 경로 생성
        File dest = new File(uploadPath + File.separator + fileName);

        // 이미지의 바이트 데이터를 경로에 지정된 폴더로 복사 -> 서버의 파일 시스템에 저장
        FileCopyUtils.copy(image.getBytes(), dest);

        // 이미지 경로 저장
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

    public void addBubbles(SiteUser user, int bubble) {
        user.setBubble(user.getBubble() + bubble);
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
        user.setPassword(passwordEncoder.encode("admin"));
        this.userRepository.save(user);
    }

    public void deleteUser(SiteUser user) {
        this.userRepository.delete(user);
    }
}
