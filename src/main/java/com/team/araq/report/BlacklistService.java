package com.team.araq.report;

import lombok.RequiredArgsConstructor;
import org.apache.poi.sl.draw.geom.GuideIf;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final BlacklistRepository blacklistRepository;

    public void saveBlacklist(String phoneNum, String reason) {
        Blacklist blacklist = new Blacklist();
        blacklist.setPhoneNum(phoneNum);
        blacklist.setReason(reason);
        blacklist.setDate(LocalDateTime.now());
        this.blacklistRepository.save(blacklist);
    }

    public Blacklist checkBlacklist(String phoneNum) {
        Optional<Blacklist> blacklist = this.blacklistRepository.findByPhoneNum(phoneNum);
        return blacklist.orElse(null);
    }

}
