package com.team.araq;

import com.team.araq.inquiry.Inquiry;
import com.team.araq.inquiry.InquiryRepository;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class AraqApplicationTests {

    @Autowired
    private InquiryRepository InquiryRepository;

    @Autowired
    private UserService userService;

    @Test
    void contextLoads() throws IOException {
        Resource resource = new ClassPathResource("static/realistic_dummy_data.xlsx");
        InputStream file = resource.getInputStream();
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        List<Inquiry> entities = new ArrayList<>();

        SiteUser user = this.userService.getByUsername("dbstjwjd");

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            Inquiry inquiry = new Inquiry();
            inquiry.setCategory(row.getCell(0).getStringCellValue());
            inquiry.setTitle(row.getCell(1).getStringCellValue());
            inquiry.setContent(row.getCell(2).getStringCellValue());
            inquiry.setVisibility(row.getCell(3).getStringCellValue());
            inquiry.setStatus("답변 대기");
            inquiry.setWriter(user);
            inquiry.setCreateDate(LocalDateTime.now());

            entities.add(inquiry);
        }
        InquiryRepository.saveAll(entities);
        workbook.close();
        file.close();
    }
}
