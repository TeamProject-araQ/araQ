package com.team.araq.inquiry;

import com.team.araq.board.post.Post;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    private String uploadPath = "C:/uploads/inquiry";

    private Specification<Inquiry> search(String kw) {
        return new Specification<Inquiry>() {
            @Override
            public Predicate toPredicate(Root<Inquiry> inquiry, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                query.distinct(true);
                Join<Inquiry, SiteUser> u1 = inquiry.join("writer", JoinType.LEFT);
                return criteriaBuilder.or(criteriaBuilder.like(inquiry.get("content"), "%" + kw + "%"),
                        criteriaBuilder.like(inquiry.get("title"), "%" + kw + "%"),
                        criteriaBuilder.like(u1.get("nickName"), "%" + kw + "%"));
            }
        };
    }

    public Page<Inquiry> getList(int page, String kw) {
        List<Sort.Order> sort = new ArrayList<>();
        sort.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sort));
        Specification<Inquiry> spec = search(kw);
        return this.inquiryRepository.findAll(spec, pageable);
    }

    public Inquiry createInquiry(InquiryDTO inquiryDTO, SiteUser user) {
        Inquiry inquiry = new Inquiry();
        inquiry.setCategory(inquiryDTO.getCategory());
        inquiry.setTitle(inquiryDTO.getTitle());
        inquiry.setContent(inquiryDTO.getContent());
        inquiry.setCreateDate(LocalDateTime.now());
        inquiry.setStatus("답변 대기");
        inquiry.setVisibility(inquiryDTO.getVisibility());
        inquiry.setWriter(user);
        return this.inquiryRepository.save(inquiry);
    }

    public void uploadImage(Inquiry inquiry, MultipartFile[] files) throws IOException {
        if (inquiry.getFiles() == null) inquiry.setFiles(new ArrayList<>());
        File uploadDirectory = new File(uploadPath);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }
        if (files != null) {
            for (MultipartFile file : files) {
                String fileName = inquiry.getId() + "_" + file.getOriginalFilename();
                File dest = new File(uploadPath + File.separator + fileName);
                FileCopyUtils.copy(file.getBytes(), dest);
                inquiry.getFiles().add("/inquiry/image/" + fileName);
            }
        }
        this.inquiryRepository.save(inquiry);
    }

    public Inquiry getInquiry(Integer id) {
        Optional<Inquiry> inquiry = this.inquiryRepository.findById(id);
        if (inquiry.isPresent()) return inquiry.get();
        else throw new RuntimeException("그런 문의 없습니다.");
    }
}
