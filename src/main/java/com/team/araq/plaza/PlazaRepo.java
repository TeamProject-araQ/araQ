package com.team.araq.plaza;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlazaRepo extends JpaRepository<Plaza, Integer> {
    Optional<Plaza> findByCode(String code);
}
