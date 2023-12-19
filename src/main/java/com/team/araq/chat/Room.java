package com.team.araq.chat;

import com.team.araq.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    private SiteUser participant1;
    @ManyToOne
    private SiteUser participant2;
    @OneToMany(mappedBy = "room", cascade = CascadeType.REMOVE)
    private List<Chat> chats;
    private String code;
    private LocalDateTime createDate;
}
