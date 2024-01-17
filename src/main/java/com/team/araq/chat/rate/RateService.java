package com.team.araq.chat.rate;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RateService {

    private final RateRepository rateRepository;

    public void saveRate(SiteUser user1, SiteUser user2, double manner, double appeal, double appearance) {
        Rate rate = new Rate();
        rate.setUser1(user1);
        rate.setUser2(user2);
        rate.setManner(manner);
        rate.setAppeal(appeal);
        rate.setAppearance(appearance);
        rate.setCreateDate(LocalDateTime.now());
        this.rateRepository.save(rate);
    }

    public Rate checkRate(SiteUser user1, SiteUser user2) {
        Optional<Rate> rate = this.rateRepository.findByUser1AndUser2(user1, user2);
        return rate.orElse(null);
    }

    public List<Rate> getRateByUser(SiteUser user) {
        return this.rateRepository.findByUser2(user);
    }

}
