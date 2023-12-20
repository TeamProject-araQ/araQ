package com.team.araq.review;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public Page<Review> getList(int page, String sortBy) {
        List<Sort.Order> sort = new ArrayList<>();
        sort.add(Sort.Order.desc(sortBy));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sort));
        return this.reviewRepository.findAll(pageable);
    }

    public void createReview(ReviewDTO reviewDTO, SiteUser user) {
        Review review = new Review();
        review.setAnswer1(reviewDTO.getAnswer1());
        review.setAnswer2(reviewDTO.getAnswer2());
        review.setAnswer3(reviewDTO.getAnswer3());
        review.setAnswer4(reviewDTO.getAnswer4());
        review.setAnswer5(reviewDTO.getAnswer5());
        review.setStar(reviewDTO.getStar());
        review.setWriter(user);
        review.setCreateDate(LocalDateTime.now());
        this.reviewRepository.save(review);
    }

    public void modifyReview(Review review, ReviewDTO reviewDTO) {
        review.setAnswer1(reviewDTO.getAnswer1());
        review.setAnswer2(reviewDTO.getAnswer2());
        review.setAnswer3(reviewDTO.getAnswer3());
        review.setAnswer4(reviewDTO.getAnswer4());
        review.setAnswer5(reviewDTO.getAnswer5());
        review.setStar(reviewDTO.getStar());
        review.setModifyDate(LocalDateTime.now());
        this.reviewRepository.save(review);
    }

    public void deleteReview(Review review) {
        this.reviewRepository.delete(review);
    }

    public Review getReview(Integer id) {
        Optional<Review> review = this.reviewRepository.findById(id);
        if (review.isPresent()) return review.get();
        else throw new RuntimeException("그런 리뷰 없습니다.");
    }
}
