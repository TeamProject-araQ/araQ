package com.team.araq.review;

import com.team.araq.user.SiteUser;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private String uploadPath = "C:/uploads/review";

    private Specification<Review> search(String kw) {
        return new Specification<Review>() {
            @Override
            public Predicate toPredicate(Root<Review> review, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                query.distinct(true);
                Join<Review, SiteUser> u1 = review.join("writer", JoinType.LEFT);
                return criteriaBuilder.or(criteriaBuilder.like(review.get("answer1"), "%" + kw + "%"),
                        criteriaBuilder.like(review.get("answer2"), "%" + kw + "%"),
                        criteriaBuilder.like(review.get("answer3"), "%" + kw + "%"),
                        criteriaBuilder.like(review.get("answer4"), "%" + kw + "%"),
                        criteriaBuilder.like(review.get("answer5"), "%" + kw + "%"),
                        criteriaBuilder.like(u1.get("nickName"), "%" + kw + "%"));
            }
        };
    }

    public Page<Review> getList(int page, String sortBy) {
        List<Sort.Order> sort = new ArrayList<>();
        sort.add(Sort.Order.desc(sortBy));
        Pageable pageable = PageRequest.of(page, 12, Sort.by(sort));
        return this.reviewRepository.findAll(pageable);
    }

    public Page<Review> getAll(int page, String kw) {
        List<Sort.Order> sort = new ArrayList<>();
        sort.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sort));
        Specification<Review> specification = search(kw);
        return this.reviewRepository.findAll(specification, pageable);
    }

    public Review createReview(ReviewDTO reviewDTO, SiteUser user) {
        Review review = new Review();
        review.setAnswer1(reviewDTO.getAnswer1());
        review.setAnswer2(reviewDTO.getAnswer2());
        review.setAnswer3(reviewDTO.getAnswer3());
        review.setAnswer4(reviewDTO.getAnswer4());
        review.setAnswer5(reviewDTO.getAnswer5());
        review.setStar(reviewDTO.getStar());
        review.setWriter(user);
        review.setCreateDate(LocalDateTime.now());
        return this.reviewRepository.save(review);
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

    public void uploadImage(Review review, MultipartFile image) throws IOException {
        if (image.isEmpty()) review.setImage("/image/defaultReview.jpg");
        else {
            File uploadDirectory = new File(uploadPath);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }
            String fileName = review.getId() + "_" + image.getOriginalFilename();
            File dest = new File(uploadPath + File.separator + fileName);
            FileCopyUtils.copy(image.getBytes(), dest);
            review.setImage("/review/image/" + fileName);
        }
        this.reviewRepository.save(review);
    }
}
