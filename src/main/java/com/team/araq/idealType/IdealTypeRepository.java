package com.team.araq.idealType;

import com.team.araq.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdealTypeRepository extends JpaRepository<IdealType, Integer> {

    Optional<IdealType> findByUser(SiteUser user);
}
