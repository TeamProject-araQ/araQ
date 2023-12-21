package com.team.araq.inquiry;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class InquiryDTO {

    private String title;

    private String content;

    private String category;

    private String visibility;

}
