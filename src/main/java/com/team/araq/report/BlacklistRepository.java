package com.team.araq.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlacklistRepository extends JpaRepository<Blacklist, Integer> {

    Optional<Blacklist> findByPhoneNum(String phoneNum);
}
