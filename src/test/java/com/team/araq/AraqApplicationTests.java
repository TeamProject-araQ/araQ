package com.team.araq;

import com.team.araq.inquiry.Inquiry;
import com.team.araq.inquiry.InquiryRepository;
import com.team.araq.user.SiteUser;
import com.team.araq.user.UserRepository;
import com.team.araq.user.UserRole;
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
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void contextLoads() throws IOException {
        Resource resource = new ClassPathResource("static/dummy_users.xlsx");
        InputStream file = resource.getInputStream();
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        List<SiteUser> entities = new ArrayList<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            SiteUser user = new SiteUser();
            user.setGender(row.getCell(0).getStringCellValue());
            user.setAge(String.valueOf((int)row.getCell(1).getNumericCellValue()));
            user.setHeight(String.valueOf((int)row.getCell(2).getNumericCellValue()));
            user.setEducation(row.getCell(3).getStringCellValue());
            user.setDrinking(row.getCell(4).getStringCellValue());
            user.setSmoking(row.getCell(5).getStringCellValue());
            user.setMbti(row.getCell(6).getStringCellValue());
            user.setReligion(row.getCell(7).getStringCellValue());
            user.setUsername(row.getCell(8).getStringCellValue());
            user.setName(row.getCell(9).getStringCellValue());
            user.setEmail(row.getCell(11).getStringCellValue());
            user.setNickName(row.getCell(12).getStringCellValue());
            user.setAddress(row.getCell(13).getStringCellValue());
            user.setHobby(row.getCell(14).getStringCellValue());
            user.setIntroduce(row.getCell(15).getStringCellValue());
            user.setRole(UserRole.USER);
            user.setCreateDate(LocalDateTime.now());

            entities.add(user);
        }
        userRepository.saveAll(entities);
        workbook.close();
        file.close();
    }
}
