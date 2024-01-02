package com.team.araq.pay;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;

    public void saveHistory(SiteUser user, int spent, int balance, String reason) {
        History history = new History();
        history.setUser(user);
        history.setSpent(spent);
        history.setBalance(balance);
        history.setReason(reason);
        history.setDate(LocalDateTime.now());
        this.historyRepository.save(history);
    }
}
