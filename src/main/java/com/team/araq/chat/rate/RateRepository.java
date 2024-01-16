package com.team.araq.chat.rate;

import com.team.araq.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RateRepository extends JpaRepository<Rate, Integer> {

    Optional<Rate> findByUser1AndUser2(SiteUser user1, SiteUser user2);
}
