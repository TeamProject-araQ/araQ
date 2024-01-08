package com.team.araq.friend;

import com.team.araq.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Integer> {

    Optional<Friend> findBySenderAndReceiver(SiteUser user1, SiteUser user2);

    @Query("SELECT f FROM Friend f WHERE (f.sender = :user OR f.receiver = :user) AND f.status = :status")
    List<Friend> findFriendsByUserAndStatus(@Param("user") SiteUser user, @Param("status") boolean status);

}
