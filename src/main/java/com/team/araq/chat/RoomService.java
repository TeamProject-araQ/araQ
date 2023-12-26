package com.team.araq.chat;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepo roomRepo;

    public void create(String uuid, SiteUser user1, SiteUser user2) {
        Room room = new Room();
        room.setParticipant1(user1);
        room.setParticipant2(user2);
        room.setCreateDate(LocalDateTime.now());
        room.setRecentDate(LocalDateTime.now());
        room.setCode(uuid);
        roomRepo.save(room);
    }

    public Room get(String code) {
        Optional<Room> room = roomRepo.findByCode(code);
        if (room.isPresent()) return room.get();
        throw new RuntimeException("해당 채팅방을 찾을 수 없습니다.");
    }

    public List<Room> getList(SiteUser user) {
        return roomRepo.findByParticipant1OrParticipant2(user, user);
    }

    public boolean check(Room room, SiteUser user) {
        return user.getUsername().equals(room.getParticipant1().getUsername()) ||
                user.getUsername().equals(room.getParticipant2().getUsername());
    }

    public void delete(Room room) {
        roomRepo.delete(room);
    }

    public void setRecent(Room room, LocalDateTime recentDate) {
        room.setRecentDate(recentDate);
        roomRepo.save(room);
    }
}
