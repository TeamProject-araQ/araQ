package com.team.araq.user;

import com.team.araq.idealType.IdealType;
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
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final TaskScheduler taskScheduler;

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

        String userUploadPath = uploadPath + "/" + user.getUsername();

        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                File uploadDirectory = new File(userUploadPath);
                if (!uploadDirectory.exists()) {
                    uploadDirectory.mkdirs();
                }
                // 새로운 이미지 저장
                String fileExtension = StringUtils.getFilenameExtension(image.getOriginalFilename());
//                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String timeStamp = Instant.now().toString().replace(":", "-").replace("-", "");
                String fileName = user.getUsername() + "_" + timeStamp + "." + fileExtension;
                File dest = new File(userUploadPath + File.separator + fileName);
                FileCopyUtils.copy(image.getBytes(), dest);

                // 이미지 경로를 사용자의 이미지 목록에 추가
                updatedImages.add("/user/image/" + user.getUsername() + "/" + fileName);
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

    public SiteUser edit(SiteUser user, UserUpdateForm userUpdateForm) throws IOException {
        user.setNickName(userUpdateForm.getNickName());
        user.setAddress(userUpdateForm.getAddress());
        user.setAge(userUpdateForm.getAge());
        user.setHeight(userUpdateForm.getHeight());
        user.setReligion(userUpdateForm.getReligion());
        user.setDrinking(userUpdateForm.getDrinking());
        user.setSmoking(userUpdateForm.getSmoking());
        user.setEducation(userUpdateForm.getEducation());
        user.setMbti(userUpdateForm.getMbti());
        user.setHobby(userUpdateForm.getHobby());
        user.setCreateDate(LocalDateTime.now());
        user.setGender(userUpdateForm.getGender());
        user.setIntroduce(userUpdateForm.getIntroduce());
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

    public void logout(List<SiteUser> users) {
        for (SiteUser user : users) {
            user.setLogin(false);
            user.setLocation("");
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
        user.setImage("/image/user/admin.png");
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

    public List<SiteUser> getListByPreference(String gender) {
        return this.userRepository.findByGenderNotAndPreferenceRandomly(gender, true);
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
        String userUploadPath = uploadPath + "/" + user.getUsername();
        for (Iterator<String> iterator = images.iterator(); iterator.hasNext(); ) {
            String image = iterator.next();
            if (image.equals(imageUrl)) {

                String imageName = Paths.get(imageUrl).getFileName().toString(); // 이미지 파일의 이름만 추출
                String deleteImage = userUploadPath + "/" + imageName;
                try {
                    Files.deleteIfExists(Path.of(deleteImage));
                    iterator.remove();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        String userUploadPath = uploadPath + "/" + user.getUsername();
        File uploadDirectory = new File(userUploadPath);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        String fileExtension = StringUtils.getFilenameExtension(image.getOriginalFilename());
        String timeStamp = Instant.now().toString().replace(":", "-").replace("-", "");
        String fileName = user.getUsername() + "_" + timeStamp + "." + fileExtension;
        File dest = new File(userUploadPath + File.separator + fileName);
        try {
            // 파일 복사
            FileCopyUtils.copy(image.getBytes(), dest);

            List<String> updatedImages = user.getImages();
            // 이미지 경로를 사용자의 이미지 목록에 추가
            updatedImages.add("/user/image/" + user.getUsername() + "/" + fileName);

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

    public void useBubble(SiteUser user, int bubble) {
        user.setBubble(user.getBubble() - bubble);
        this.userRepository.save(user);
    }

    public List<SiteUser> getByIdealType(IdealType idealType, String gender) {
        return this.userRepository.findMatchingUsersByIdealType(idealType, gender);
    }

    public List<SiteUser> getBySmoking(String gender, String smoking) {
        return this.userRepository.findByGenderNotAndSmoking(gender, smoking);
    }

    public List<SiteUser> getByDrinking(String gender, String drinking) {
        return this.userRepository.findByGenderNotAndDrinking(gender, drinking);
    }

    public List<SiteUser> getByHobby(String gender, String hobby) {
        return this.userRepository.findByGenderNotAndHobbyContaining(gender, hobby);
    }

    public List<SiteUser> getByMbti(String gender, String mbti) {
        return this.userRepository.findByGenderNotAndMbti(gender, mbti);
    }

    public List<SiteUser> getByReligion(String gender, String religion) {
        return this.userRepository.findByGenderNotAndReligion(gender, religion);
    }

    public List<SiteUser> getByPersonalities(SiteUser loginUser) {
        List<SiteUser> matchingUsers = new ArrayList<>();
        List<SiteUser> userList = this.userRepository.findByGenderNot(loginUser.getGender());
        for (SiteUser user : userList) {
            List<String> userPersonality = user.getPersonality();
            int matchCount = 0;
            for (String personality : loginUser.getPersonality()) {
                if (userPersonality.contains(personality)) {
                    matchCount++;
                }
            }
            if (matchCount >= 2)
                matchingUsers.add(user);
        }
        return matchingUsers;
    }

    public void setUserLocationInPlaza(SiteUser user, String top, String left) {
        user.setLocationTop(top);
        user.setLocationLeft(left);
        userRepository.save(user);
    }

    public void setFocusInPlaza(SiteUser user, String status) {
        user.setPlazaFocus(status);
        userRepository.save(user);
    }

    public List<String> getUserPersonality(SiteUser user) {
        List<String> userPersonality = user.getPersonality();
        return userPersonality;
    }

    public void savePersonality(SiteUser user, List<String> personality) {
        user.setPersonality(personality);
        userRepository.save(user);
    }

    public void setLocation(SiteUser user, String location) {
        user.setLocation(location);
        userRepository.save(user);
    }

    public List<SiteUser> getByLocation(String location) {
        return userRepository.findByLocation(location);
    }


    public boolean checkUsername(String username) {
        Optional<SiteUser> user = userRepository.findByusername(username);
        return user.isEmpty();
    }

    public boolean checkEmail(String email) {
        Optional<SiteUser> user = userRepository.findByEmail(email);
        return user.isEmpty();
    }

    public void getPreference(SiteUser user, int days) {
        if (days == 1) {
            user.setPreference1Day(true);
        } else if (days == 7) {
            user.setPreference7Day(true);
        } else if (days == 30) {
            user.setPreference30Day(true);
        }
        user.setPreference(true);
        user.setGetPreferenceTime(LocalDateTime.now());
        this.userRepository.save(user);
    }

    public void getListenVoice(SiteUser user, int days) {
        if (days == 1) {
            user.setListenVoice1Day(true);
        } else if (days == 7) {
            user.setListenVoice7Day(true);
        } else if (days == 30) {
            user.setListenVoice30Day(true);
        }
        user.setListenVoice(true);
        user.setGetListenVoice(LocalDateTime.now());
        this.userRepository.save(user);
    }

    public void addAraQPass(SiteUser user, int pass) {
        user.setAraQPass(user.getAraQPass() + pass);
        this.userRepository.save(user);
    }

    public void useAraQPass(SiteUser user) {
        user.setAraQPass(user.getAraQPass() - 1);
        this.userRepository.save(user);
    }

    public void addChatPass(SiteUser user, int pass) {
        user.setChatPass(user.getChatPass() + pass);
        this.userRepository.save(user);
    }

    public void useChatPass(SiteUser user) {
        user.setChatPass(user.getChatPass() - 1);
        this.userRepository.save(user);
    }

    public void changeColor(SiteUser user, String background, String color) {
        user.setChatBackground(background);
        user.setChatColor(color);
        user.setGetChatColor(LocalDateTime.now());
        this.userRepository.save(user);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void checkPurchaseTime() {
        LocalDateTime now = LocalDateTime.now();
        List<SiteUser> users = this.userRepository.findAll();
        for (SiteUser user : users) {
            LocalDateTime getPreferenceTime = user.getGetPreferenceTime();
            LocalDateTime getListenVoiceTime = user.getGetListenVoice();
            LocalDateTime getChatColorTime = user.getGetChatColor();
            if (getPreferenceTime != null) {
                if (user.isPreference1Day() && getPreferenceTime.plusDays(1).isBefore(now) ||
                        user.isPreference7Day() && getPreferenceTime.plusDays(7).isBefore(now) ||
                        user.isPreference30Day() && getPreferenceTime.plusDays(30).isBefore(now)) {
                    user.setPreference(false);
                    user.setPreference1Day(false);
                    user.setPreference7Day(false);
                    user.setPreference30Day(false);
                    user.setGetPreferenceTime(null);
                }
            } else if (getListenVoiceTime != null) {
                if (user.isListenVoice1Day() && getListenVoiceTime.plusDays(1).isBefore(now) ||
                        user.isListenVoice7Day() && getListenVoiceTime.plusDays(7).isBefore(now) ||
                        user.isListenVoice30Day() && getListenVoiceTime.plusDays(30).isBefore(now)) {
                    user.setListenVoice(false);
                    user.setListenVoice1Day(false);
                    user.setListenVoice7Day(false);
                    user.setListenVoice30Day(false);
                    user.setGetListenVoice(null);
                }
            } else if (getChatColorTime != null) {
                if (getChatColorTime.plusDays(7).isBefore(now)) {
                    user.setChatColor(null);
                    user.setChatBackground(null);
                    user.setGetChatColor(null);
                }
            }
            this.userRepository.save(user);
        }
    }


    public void savePhoneNum(SiteUser user, String phoneNum) {
        user.setPhoneNum(phoneNum);
        this.userRepository.save(user);
    }
}