package com.team.araq.user;

import com.team.araq.board.Comment.Comment;
import com.team.araq.board.Post.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    private String password;

    // 별명
    private String nickName;

    @Column(unique = true)
    private String email;

    private String phoneNum;

    private String address;

    @OneToMany(mappedBy = "writer", cascade = CascadeType.REMOVE)
    private List<Post> postList;

    @OneToMany(mappedBy = "writer", cascade = CascadeType.REMOVE)
    private List<Comment> commentList;
}
