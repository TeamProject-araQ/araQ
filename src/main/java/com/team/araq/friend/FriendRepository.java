package com.team.araq.friend;

import com.team.araq.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Integer> {

    Optional<Friend> findBySenderAndReceiver(SiteUser user1, SiteUser user2);

    List<Friend> findBySenderOrReceiverAndStatus(SiteUser user, SiteUser sameUser, boolean status);

    List<Friend> findByReceiverAndStatus(SiteUser user, boolean status);
}
