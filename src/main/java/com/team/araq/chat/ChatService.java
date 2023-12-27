package com.team.araq.chat;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepo chatRepo;

    public Chat create(Room room, SiteUser writer, SiteUser target, String content, LocalDateTime recentDate) {
        Chat chat = new Chat();
        chat.setRoom(room);
        chat.setWriter(writer);
        chat.setTarget(target);
        chat.setContent(content);
        chat.setCreateDate(LocalDateTime.now());
        chat.setConfirm(0);
        chat.setDifDate(!recentDate.toLocalDate().equals(LocalDate.now()));
        return chatRepo.save(chat);
    }

    public void confirm(Chat chat) {
        chat.setConfirm(1);
        chatRepo.save(chat);
    }

    public void confirm(List<Chat> chats) {
        for (Chat chat : chats) {
            chat.setConfirm(1);
            chatRepo.save(chat);
        }
    }

    public List<Chat> getByRoomAndWriter(Room room, SiteUser writer) {
        return chatRepo.findByRoomAndWriter(room, writer);
    }

    public void setImages(Chat chat, List<String> images) {
        chat.setImages(images);
        chatRepo.save(chat);
    }
}
