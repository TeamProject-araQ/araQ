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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private String uploadPath = "C:/uploads/user";

    private String audioPath = "C:/uploads/audio";


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


    public SiteUser create(UserCreateForm userCreateForm) throws IOException {
        SiteUser user = new SiteUser();
        user.setUsername(userCreateForm.getUsername());
        user.setEmail(userCreateForm.getEmail());
        user.setPassword(passwordEncoder.encode(userCreateForm.getPassword1()));
        user.setName(userCreateForm.getName());
        user.setPhoneNum(userCreateForm.getPhoneNum());
        userRepository.save(user);
        return user;
    }

    public SiteUser update(SiteUser user, UserUpdateForm userUpdateForm) throws IOException {
        user.setNickName(userUpdateForm.getNickName());
        user.setAddress(userUpdateForm.getAddress());
        user.setAge(userUpdateForm.getAge());
        user.setHeight(userUpdateForm.getHeight());
        user.setReligion(userUpdateForm.getReligion());
        user.setDrinking(userUpdateForm.getDrinking());
        user.setSmoking(userUpdateForm.getSmoking());
        user.setEducation(userUpdateForm.getEducation());
        user.setMbti(userUpdateForm.getMbti());
        user.setPersonality(userUpdateForm.getPersonality());
        user.setHobby(userUpdateForm.getHobby());
        user.setCreateDate(LocalDateTime.now());
        user.setGender(userUpdateForm.getGender());
        user.setIntroduce(userUpdateForm.getIntroduce());

        List<MultipartFile> images = userUpdateForm.getImages();
        List<String> updatedImages = new ArrayList<>();

            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    File uploadDirectory = new File(uploadPath);
                    if (!uploadDirectory.exists()) {
                        uploadDirectory.mkdirs();
                    }
                    // 새로운 이미지 저장
                    String fileExtension = StringUtils.getFilenameExtension(image.getOriginalFilename());
                    String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                    String fileName = user.getUsername() + "_" + timeStamp + "." + fileExtension;
                    File dest = new File(uploadPath + File.separator + fileName);
                    FileCopyUtils.copy(image.getBytes(), dest);

                    // 이미지 경로를 사용자의 이미지 목록에 추가
                    updatedImages.add("/user/image/" + fileName);
                }
            }
            // 새로운 이미지 목록으로 업데이트
            if (!updatedImages.isEmpty()) {
                user.setImages(updatedImages);
                user.setImage(updatedImages.get(0));
            } else {
                // 이미지를 업로드하지 않은 경우 기본 이미지 설정
                List<String> defaultImages = new ArrayList<>();
                if (userUpdateForm.getGender().equals("남성")) {
                    defaultImages.add("/profile/default-m.jpg");
                } else {
                    defaultImages.add("/profile/default-w.jpg");
                }
                user.setImages(defaultImages);
                user.setImage(defaultImages.get(0));
            }
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

    public void login(SiteUser user) {
        user.setLogin(true);
        userRepository.save(user);
    }

    public void logout(SiteUser user) {
        user.setLogin(false);
        userRepository.save(user);
    }

    public void logout(List<SiteUser> users) {
        for (SiteUser user : users) {
            user.setLogin(false);
            userRepository.save(user);
        }
    }

    public List<SiteUser> getLoginUsers() {
        return userRepository.findByLogin(true);
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
        user.setImage("/image/admin.png");
        user.setPassword(passwordEncoder.encode("admin"));
        this.userRepository.save(user);
    }

    public void deleteUser(SiteUser user) {
        this.userRepository.delete(user);
    }

    public boolean checkPassword(SiteUser user, String currentPassword) {
        if (passwordEncoder.matches(currentPassword, user.getPassword())) {
            return true;
        } else {
            return false;
        }
    }

    public void updatePw(SiteUser user, String newPw, String confirmPw) {
        if (newPw.equals(confirmPw)) {
            user.setPassword(passwordEncoder.encode(newPw));
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("새로운 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }
    }

    public boolean checkUser(SiteUser user, String username, String password) {
        if (user.getUsername().equals(username) && passwordEncoder.matches(password, user.getPassword())) {
            return true;
        } else {
            return false;
        }
    }

    public SiteUser findUsernameByEmail(String email) {
        Optional<SiteUser> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return user.get();
        }
        throw new RuntimeException("그런 사람 없습니다.");
    }

    public SiteUser getByUserToken(String token) {
        Optional<SiteUser> user = userRepository.findByToken(token);
        if (user.isPresent()) {
            return user.get();
        }
        throw new UsernameNotFoundException("그런 사람 없습니다.");
    }

    public SiteUser getByNameAndPhoneNum(String name, String phoneNum) {
        Optional<SiteUser> user = userRepository.findByNameAndPhoneNum(name, phoneNum);
        if (user.isPresent()) {
            return user.get();
        }
        throw new UsernameNotFoundException("그런 사람 없습니다.");
    }

    public List<SiteUser> getRandomList(String gender) {
        List<SiteUser> userList = this.userRepository.findByGenderNot(gender);
        Collections.shuffle(userList);
        return userList.size() > 3 ? userList.subList(0, 3) : userList;
    }

    public void uploadAudio(MultipartFile multipartFile, SiteUser user) {
        File uploadDirectory = new File(audioPath);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }
        try {
            String fileName = user.getUsername() + "_" + multipartFile.getOriginalFilename();
            File dest = new File(audioPath + File.separator + fileName);
            multipartFile.transferTo(dest);
            user.setAudio("/user/audio/" + fileName);
            this.userRepository.save(user);

        } catch (IOException ex) {
            throw new RuntimeException("파일 저장 실패: " + multipartFile.getOriginalFilename(), ex);
        }
    }

    public void deleteImage(List<String> images, String imageUrl, SiteUser user) {
        for (Iterator<String> iterator = images.iterator(); iterator.hasNext(); ) {
            String image = iterator.next();
            if (image.equals(imageUrl)) {
                iterator.remove();
            }
        }
        if (user.getImage() != null && user.getImage().equals(imageUrl)) {
            if (user.getGender().equals("남성")) {
                user.setImage("/profile/default-m.jpg");
            } else if (user.getGender().equals("여성")) {
                user.setImage("/profile/default-w.jpg");
            }
        }
        user.setImages(images);
        userRepository.save(user);
    }

    public void setProfileImage(String imageUrl, SiteUser user) {
        user.setImage(imageUrl);
        userRepository.save(user);
    }

    public List<String> addImage(MultipartFile image, SiteUser user) {
        String fileExtension = StringUtils.getFilenameExtension(image.getOriginalFilename());
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileName = user.getUsername() + "_" + timeStamp + "." + fileExtension;
        File dest = new File(uploadPath + File.separator + fileName);
        try {
            // 파일 복사
            FileCopyUtils.copy(image.getBytes(), dest);

            List<String> updatedImages = user.getImages();
            // 이미지 경로를 사용자의 이미지 목록에 추가
            updatedImages.add("/user/image/" + fileName);

            // 이미지 목록을 사용자 객체에 저장
            user.setImages(updatedImages);
            userRepository.save(user);
            return user.getImages();
        } catch (IOException e) {
            // IOException이 발생한 경우 처리
            e.printStackTrace(); // 또는 로그에 기록 등을 수행할 수 있음
            return null;
        }
    }
}
