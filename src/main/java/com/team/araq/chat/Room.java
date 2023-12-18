package com.team.araq.chat;

import com.team.araq.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne
    private SiteUser participant1;
    @OneToOne
    private SiteUser participant2;
    @OneToMany(mappedBy = "room", cascade = CascadeType.REMOVE)
    private List<Chat> chats;
}
