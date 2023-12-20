package com.team.araq.chat;

import com.team.araq.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Room room;
    @ManyToOne
    private SiteUser writer;
    @ManyToOne
    private SiteUser target;
    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime createDate;
}
