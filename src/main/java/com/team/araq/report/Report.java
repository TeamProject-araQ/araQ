package com.team.araq.report;

import com.team.araq.chat.Room;
import com.team.araq.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private SiteUser reportedUser;

    @ManyToOne
    private SiteUser reportingUser;

    private String reason;

    private String detailReason;

    private LocalDateTime reportDate;

    private String status;

    @ManyToOne
    private Room room;

}
