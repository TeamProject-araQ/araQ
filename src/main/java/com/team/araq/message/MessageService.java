package com.team.araq.message;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public Message sendMessage(SiteUser sender, SiteUser receiver, String content) {
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setStatus(false);
        message.setDateTime(LocalDateTime.now());
        return this.messageRepository.save(message);
    }

    public Page<Message> getListByReceiver(SiteUser receiver, String keyword, int page) {
        List<Sort.Order> sort = new ArrayList<>();
        sort.add(Sort.Order.desc("dateTime"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sort));
        return this.messageRepository.findByReceiver(receiver, keyword, pageable);
    }

    public Page<Message> getListBySender(SiteUser sender, String keyword, int page) {
        List<Sort.Order> sort = new ArrayList<>();
        sort.add(Sort.Order.desc("dateTime"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sort));
        return this.messageRepository.findBySender(sender, keyword, pageable);
    }

    public void updateStatus(Message message) {
        message.setStatus(true);
        this.messageRepository.save(message);
    }

    public Message getMessage(Integer id) {
        Optional<Message> message = this.messageRepository.findById(id);
        if (message.isPresent()) return message.get();
        else throw new RuntimeException("그런 쪽지 없습니다.");
    }
}
