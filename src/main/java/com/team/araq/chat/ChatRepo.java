package com.team.araq.chat;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepo extends JpaRepository<Chat, Long> {
}
