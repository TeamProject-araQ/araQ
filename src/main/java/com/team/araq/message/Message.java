package com.team.araq.message;

import com.team.araq.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private SiteUser sender;

    @ManyToOne
    private SiteUser receiver;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime dateTime;

    private boolean status;
}
