package com.team.araq.review;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
}
