package com.team.araq.like;

import com.team.araq.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<UserLike, Integer> {

    @Query("SELECT l FROM UserLike l WHERE l.user = :user OR l.likedUser = :user")
    List<UserLike> findByUserOrLikedUser(@Param("user") SiteUser user);

    Optional<UserLike> findByUserAndLikedUser(SiteUser user1, SiteUser user2);

    @Query("SELECT COUNT(l) FROM UserLike l WHERE l.successDate BETWEEN :startDate AND :endDate")
    long countSuccessByDateRangeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(l) FROM UserLike l WHERE l.successDate BETWEEN :startDate AND :endDate OR l.failDate BETWEEN :startDate AND :endDate")
    long countByDateRangeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
