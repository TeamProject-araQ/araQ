package com.team.araq.announcement;

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
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public void createAnnouncement(String title, String content) {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setCreateDate(LocalDateTime.now());
        this.announcementRepository.save(announcement);
    }

    public Page<Announcement> getList(int page, String kw) {
        List<Sort.Order> sort = new ArrayList<>();
        sort.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sort));
        return this.announcementRepository.findByTitleContainingOrContentContaining(kw, pageable);
    }

    public void deleteAnnouncement(Announcement announcement) {
        this.announcementRepository.delete(announcement);
    }

    public Announcement getAnnouncement(Integer id) {
        Optional<Announcement> announcement = this.announcementRepository.findById(id);
        if (announcement.isPresent()) return announcement.get();
        else throw new RuntimeException("그런 공지 없습니다.");
    }

    public void updateView(Announcement announcement) {
        announcement.setView(announcement.getView() + 1);
        this.announcementRepository.save(announcement);
    }
}
