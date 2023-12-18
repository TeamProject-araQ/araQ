package com.team.araq.chat;

import com.team.araq.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepo extends JpaRepository<Room, Integer> {
    Optional<Room> findByCode(String code);
    List<Room> findByParticipant1OrParticipant2(SiteUser user1, SiteUser user2);
}
