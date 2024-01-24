package com.team.araq.inquiry;

import com.team.araq.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    private String uploadPath = "uploads/inquiry";

    public Page<Inquiry> getList(int page, String kw, String category) {
        List<Sort.Order> sort = new ArrayList<>();
        sort.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sort));
        String kwLike = "%" + kw + "%";

        Page<Inquiry> pages;
        if (kw.isBlank() && category.isBlank())
            pages = this.inquiryRepository.findAll(pageable);
        else if (kw.isBlank()) pages = this.inquiryRepository.findByCategory(category, pageable);
        else if (category.isBlank())
            pages = this.inquiryRepository.findByTitleLikeOrContentLike(kwLike, kwLike, pageable);
        else pages = this.inquiryRepository.findByKeywordAndCategory(kwLike, category, pageable);

        return pages;
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
        File uploadDirectory = new File(uploadPath);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }
        if (inquiry.getFiles() == null) inquiry.setFiles(new ArrayList<>());
        for (MultipartFile file : files) {
            String fileName = inquiry.getId() + "_" + file.getOriginalFilename();
            File dest = new File(uploadPath + File.separator + fileName);
            FileCopyUtils.copy(file.getBytes(), dest);
            inquiry.getFiles().add("/inquiry/image/" + fileName);
        }
        this.inquiryRepository.save(inquiry);
    }

    public Inquiry getInquiry(Integer id) {
        Optional<Inquiry> inquiry = this.inquiryRepository.findById(id);
        if (inquiry.isPresent()) return inquiry.get();
        else throw new RuntimeException("그런 문의 없습니다.");
    }

    public void updateStatus(Inquiry inquiry) {
        inquiry.setStatus("답변 완료");
        this.inquiryRepository.save(inquiry);
    }

    public void deleteInquiry(Inquiry inquiry) {
        this.inquiryRepository.delete(inquiry);
    }

    public List<Inquiry> getListByWriter(SiteUser user) {
        return this.inquiryRepository.findTop3ByWriterOrderByCreateDateDesc(user);
    }
}
