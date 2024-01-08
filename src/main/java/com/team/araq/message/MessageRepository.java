package com.team.araq.message;

import com.team.araq.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("SELECT m FROM Message m WHERE m.receiver = :receiver AND (m.sender.nickName LIKE %:kw% OR m.content LIKE %:kw%)")
    Page<Message> findByReceiver(@Param("receiver") SiteUser receiver, @Param("kw") String keyword, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.sender = :sender AND (m.receiver.nickName LIKE %:kw% OR m.content LIKE %:kw%)")
    Page<Message> findBySender(@Param("sender") SiteUser sender, @Param("kw") String keyword, Pageable pageable);
}
