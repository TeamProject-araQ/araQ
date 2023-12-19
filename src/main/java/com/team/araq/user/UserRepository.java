package com.team.araq.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {
    Optional<SiteUser> findByusername(String username);
    List<SiteUser> findByAddressLikeAndGender(String address, String gender);

    Page<SiteUser> findAll(Specification<SiteUser> spec, Pageable pageable);

}
