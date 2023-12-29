package com.team.araq.like;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;

    public void likeUser(SiteUser user, SiteUser likedUser) {
        UserLike like = new UserLike();
        like.setUser(user);
        like.setLikedUser(likedUser);
        like.setStatus("매칭 대기");
        this.likeRepository.save(like);
    }

    public void acceptUser(UserLike like) {
        like.setStatus("매칭 성공");
        this.likeRepository.save(like);
    }

    public UserLike getLike(Integer id) {
        Optional<UserLike> like = this.likeRepository.findById(id);
        if (like.isPresent()) return like.get();
        else throw new RuntimeException("그런 라이크 없습니다.");
    }

    public List<UserLike> getListByUser(SiteUser user) {
        return this.likeRepository.findByUserOrLikedUser(user);
    }

    public UserLike getListForCheck(SiteUser user1, SiteUser user2) {
        Optional<UserLike> userLike = this.likeRepository.findByUserAndLikedUser(user1, user2);
        return userLike.orElse(null);
    }

    public void refuseLike(UserLike like) {
        like.setStatus("매칭 실패");
        this.likeRepository.save(like);
    }

    public String checkStatus(SiteUser user, SiteUser likedUser) {
        if (this.likeRepository.findByUserAndLikedUser(user, likedUser).isPresent()) {
            return likeRepository.findByUserAndLikedUser(user, likedUser)
                    .map(UserLike::getStatus).orElse("상태 없음");
        }
        if (this.likeRepository.findByUserAndLikedUser(likedUser, user).isPresent()) {
            return likeRepository.findByUserAndLikedUser(likedUser, user)
                    .map(UserLike::getStatus).orElse("상태 없음");
        }
        return null;
    }
}
