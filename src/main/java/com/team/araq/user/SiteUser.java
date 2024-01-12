package com.team.araq.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.team.araq.board.comment.Comment;
import com.team.araq.board.post.Post;
import com.team.araq.chat.Room;
import com.team.araq.friend.Friend;
import com.team.araq.idealType.IdealType;
import com.team.araq.inquiry.Inquiry;
import com.team.araq.like.UserLike;
import com.team.araq.message.Message;
import com.team.araq.pay.History;
import com.team.araq.pay.Payment;
import com.team.araq.report.Report;
import com.team.araq.review.Review;
import com.team.araq.taste.Taste;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
public class SiteUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 아이디
    @Column(unique = true)
    private String username;

    @JsonIgnore
    private String password;

    private String name;

    // 별명
    private String nickName;

    @Column(unique = true)
    private String email;

    // @Column(unique = true) 나중에 주석 풀기
    private String phoneNum;

    private String address;

    private String age;

    private String height;

    // 종교
    private String religion;

    // 음주
    private String drinking;

    // 흡연
    private String smoking;

    // 학력
    private String education;

    // mbti
    private String mbti;

    // 성격
    private List<String> personality;

    // 취미
    private String hobby;

    // 대표 사진
    private String image;

    @JsonIgnore
    @OneToMany(mappedBy = "writer", cascade = CascadeType.REMOVE)
    private List<Post> postList;

    @JsonIgnore
    @OneToMany(mappedBy = "writer", cascade = CascadeType.REMOVE)
    private List<Comment> commentList;

    private int bubble;

    private String gender;

    private String introduce;

    private LocalDateTime createDate;

    @JsonIgnore
    @OneToOne(mappedBy = "writer", cascade = CascadeType.REMOVE)
    private Review review;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Payment> paymentList;

    @OneToMany(mappedBy = "participant1", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Room> roomList1;

    @OneToMany(mappedBy = "participant2", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<Room> roomList2;

    @JsonIgnore
    @OneToMany(mappedBy = "writer", cascade = CascadeType.REMOVE)
    private List<Inquiry> inquiryList;

    private boolean login;

    private String token;

    @JsonIgnore
    @OneToMany(mappedBy = "reportingUser", cascade = CascadeType.REMOVE)
    private List<Report> reportingUsers;

    @JsonIgnore
    @OneToMany(mappedBy = "reportedUser", cascade = CascadeType.REMOVE)
    private List<Report> reportedUsers;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<UserLike> likeUsers;

    @JsonIgnore
    @OneToMany(mappedBy = "likedUser")
    private List<UserLike> likedUsers;

    private String audio;

    // 사진 목록
    @Column(length = 1000)
    private List<String> images;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Taste taste;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<History> historyList;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private IdealType idealType;

    private String locationTop;

    private String locationLeft;

    private String plazaFocus;

    @JsonIgnore
    @OneToMany(mappedBy = "sender", cascade = CascadeType.REMOVE)
    private List<Message> sendList;

    @JsonIgnore
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.REMOVE)
    private List<Message> receiveList;

    @JsonIgnore
    @OneToMany(mappedBy = "sender", cascade = CascadeType.REMOVE)
    private List<Friend> senderList;

    @JsonIgnore
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.REMOVE)
    private List<Friend> receiverList;

    private String location;

    private boolean preference1Day;

    private boolean preference7Day;

    private boolean preference30Day;

    private boolean preference;

    private LocalDateTime getPreferenceTime;

    private boolean listenVoice1Day;

    private boolean listenVoice7Day;

    private boolean listenVoice30Day;

    private boolean listenVoice;

    private LocalDateTime getListenVoice;

    private int araQPass;

    private int chatPass;

    private String chatBackground;

    private String chatColor;

    private LocalDateTime getChatColor;

   private boolean socialJoin;

    public SiteUser(String username, String name, String email, boolean socialJoin, LocalDateTime createDate) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.socialJoin = socialJoin;
        this.createDate = createDate;
    }

    public SiteUser() {
    }

    public SiteUser update(String name, String email) {
        this.name = name;
        this.email = email;
        return this;
    }

}
