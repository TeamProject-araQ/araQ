package com.team.araq.announcement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

    @Query("SELECT a FROM Announcement a WHERE a.title LIKE %?1% OR a.content LIKE %?1%")
    Page<Announcement> findByTitleContainingOrContentContaining(String keyword, Pageable pageable);
}
