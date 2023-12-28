package com.team.araq.like;

import com.team.araq.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeRepository extends JpaRepository<UserLike, Integer> {

    List<UserLike> findByUser(SiteUser user);

    List<UserLike> findByLikedUser(SiteUser user);

    @Query("SELECT l FROM UserLike l WHERE l.user = :user OR l.likedUser = :user")
    List<UserLike> findByUserOrLikedUser(@Param("user") SiteUser user);

    List<UserLike> findByUserAndLikedUser(SiteUser user1, SiteUser user2);
}
