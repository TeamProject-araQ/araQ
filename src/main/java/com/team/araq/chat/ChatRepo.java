package com.team.araq.chat;

import com.team.araq.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepo extends JpaRepository<Chat, Long> {
    List<Chat> findByRoomAndWriter(Room room, SiteUser writer);
}
