package com.team.araq.taste;

import com.team.araq.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TasteRepository extends JpaRepository<Taste, Integer> {

    Optional<Taste> findByUser(SiteUser user);
}
