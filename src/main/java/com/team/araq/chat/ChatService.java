package com.team.araq.chat;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepo chatRepo;

    public Chat create(Room room, SiteUser writer, SiteUser target, String content) {
        Chat chat = new Chat();
        chat.setRoom(room);
        chat.setWriter(writer);
        chat.setTarget(target);
        chat.setContent(content);
        chat.setCreateDate(LocalDateTime.now());
        chat.setConfirm(0);
        return chatRepo.save(chat);
    }

    public void confirm(Chat chat) {
        chat.setConfirm(1);
        chatRepo.save(chat);
    }
}
