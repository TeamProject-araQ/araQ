package com.team.araq.friend;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;

    public void requestFriend(SiteUser sender, SiteUser receiver) {
        Friend friend = new Friend();
        friend.setSender(sender);
        friend.setReceiver(receiver);
        friend.setStatus(false);
        this.friendRepository.save(friend);
    }

    public Friend getFriend(SiteUser sender, SiteUser receiver) {
        Optional<Friend> friend1 = this.friendRepository.findBySenderAndReceiver(sender, receiver);
        Optional<Friend> friend2 = this.friendRepository.findBySenderAndReceiver(receiver, sender);
        if (friend1.isPresent()) return friend1.get();
        else if (friend2.isPresent()) return friend2.get();
        else throw new RuntimeException("그런 친구 없습니다.");

    }
    public boolean checkFriend(SiteUser sender, SiteUser receiver) {
        Optional<Friend> friend1 = this.friendRepository.findBySenderAndReceiver(sender, receiver);
        Optional<Friend> friend2 = this.friendRepository.findBySenderAndReceiver(receiver, sender);
        if (friend1.isPresent() || friend2.isPresent()) return true;
        else return false;
    }

    public void acceptFriend(Friend friend) {
        friend.setStatus(true);
        this.friendRepository.save(friend);
    }

    public void refuseOrDeleteFriend(Friend friend) {
        this.friendRepository.delete(friend);
    }

    public List<Friend> getList(SiteUser user) {
        return this.friendRepository.findBySenderOrReceiverAndStatus(user, user, true);
    }

    public List<Friend> requestList(SiteUser user) {
        return this.friendRepository.findByReceiverAndStatus(user, false);
    }
}
